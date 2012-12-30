package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.eclipsewurstplugin.editor.outline.Icons;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import de.peeeq.wurstscript.utils.Utils;

public class WurstCompletionProcessor implements IContentAssistProcessor {

	private final WurstEditor editor;
	private int offset;
	private String alreadyEntered;
	private String errorMessage;
	private IContextInformationValidator validator;
	

	public WurstCompletionProcessor(WurstEditor editor) {
		this.editor = editor;
		this.validator = new WurstContextInformationValidator();
	}
	
	
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {'.'};
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		this.offset = offset;
		CompilationUnit cu = editor.reconcile(false);
		if (Utils.isEmptyCU(cu)) {
			errorMessage = "Could not parse file.";
			System.out.println("cu is empty ...");
			return null;
		}
		
		
		alreadyEntered = getAlreadyEnteredText(viewer, offset);
		System.out.println("already entered = " + alreadyEntered);
		
		List<ICompletionProposal> completions = Lists.newArrayList();
		
		
		
		AstElement elem =  Utils.getAstElementAtPos(cu, offset);
		System.out.println("get completions at " + Utils.printElement(elem));
		
		if (elem instanceof ExprMemberVar) {
			ExprMemberVar e = (ExprMemberVar) elem;
			WurstType leftType = e.getLeft().attrTyp();
			if (leftType instanceof WurstTypeNamedScope) {
				WurstTypeNamedScope ns = (WurstTypeNamedScope) leftType;
				Multimap<String, NameLink> visibleNames = ns.getDef().attrNameLinks();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
			} else if (leftType instanceof WurstTypeTuple) {
				WurstTypeTuple tt = (WurstTypeTuple) leftType;
				Multimap<String, NameLink> visibleNames = tt.getTupleDef().attrNameLinks();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
			}
			
			// add member vars
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
				
				completionsAddVisibleExtensionFunctions(alreadyEntered, completions, visibleNames, leftType);
				scope = scope.attrNextScope();
			}
		} else if (elem instanceof ExprRealVal) {
			// show no hints for reals
		} else if (elem instanceof WPackage) {
			// no hints at package level
		} else if (elem instanceof WImport) {
			WImport imp = (WImport) elem;
			WurstModel model = elem.getModel();
			for (WPackage p : model.attrPackagesFresh().values()) {
				if (p.getName().toLowerCase().startsWith(alreadyEntered)) {
					completions.add(makeNameDefCompletion(p));
				}
			}
		} else if (elem instanceof ExprNewObject) {
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
				for (NameLink n : visibleNames.values()) {
					if (n.getNameDef() instanceof ClassDef 
						&& n.getName().toLowerCase().startsWith(alreadyEntered)) {
						ClassDef c = (ClassDef) n.getNameDef();
						for (ConstructorDef constr : c.attrConstructors()) {
							completions.add(makeConstructorCompletion(c, constr));
						}
					}
				}
				scope = scope.attrNextScope();
			}
		} else {
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
				scope = scope.attrNextScope();
			}
		}
		removeDuplicates(completions);
		
		if (completions.size() > 0) {
			return completions.toArray(new ICompletionProposal[completions.size()]);
		}
		errorMessage = null;
		return null;
	}

	


	private void removeDuplicates(List<ICompletionProposal> cs) {
		for (int i=0; i<cs.size()-1; i++) {
			String displayStringI = firstPartOfDisplayString(cs.get(i).getDisplayString());
			for (int j=cs.size()-1; j>i; j--) {				
				String displayStringJ =firstPartOfDisplayString(cs.get(j).getDisplayString());
				if (displayStringI.equals(displayStringJ)) {
					cs.remove(j);
				}
			}
		}
		
	}


	private String firstPartOfDisplayString(String s) {
		int p = s.indexOf("-");
		if (p < 0) {
			return s;
		}
		return s.substring(0,p);
	}


	private String getAlreadyEnteredText(ITextViewer viewer, int offset) {
		try {
			int start = offset-1;
			IDocument doc = viewer.getDocument();
			while (start >= 0) {
				char c = doc.getChar(start);
				if (!Character.isJavaIdentifierPart(c)) break;
				start--;
			}
			start++;
			return doc.get(start, offset-start).toLowerCase();
		} catch (BadLocationException e) {
		}
		return "";
	}


	private void completionsAddVisibleNames(String alreadyEntered,
			List<ICompletionProposal> completions, Multimap<String, NameLink> visibleNames) {
		for (Entry<String, NameLink> e : visibleNames.entries()) {
			if (!e.getKey().toLowerCase().startsWith(alreadyEntered)) {
				continue;
			}
			if (e.getValue().getNameDef() instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) e.getValue().getNameDef();
				
				ICompletionProposal completion = makeFunctionCompletion(f);
				completions.add(completion);
			} else {
				completions.add(makeNameDefCompletion(e.getValue().getNameDef()));
			}
			
		}
	}
	
	private ICompletionProposal makeNameDefCompletion(NameDef n) {
		String replacementString = n.getName();
		int replacementOffset = offset - alreadyEntered.length();
		int replacementLength = alreadyEntered.length();
		int cursorPosition = replacementString.length();
		Image image = Icons.var;
		RGB rbg  = new RGB(255, 255, 222);
		
		String comment = n.attrComment();
		comment = comment.replaceAll("\n", "<br />");
		
		String displayString = n.getName() + " : " + n.attrTyp().getFullName() + " - [" + nearestScopeName(n) +"]";
		IContextInformation contextInformation= new ContextInformation(
				n.getName(), Utils.printElement(n)+" : " + n.attrTyp().getFullName() + " -  defined in " + nearestScopeName(n)); //$NON-NLS-1$
		String additionalProposalInfo;
		if (n.attrComment().length() > 1) {
			additionalProposalInfo = comment;
		}else{
			additionalProposalInfo = "<i>No hotdoc provided</i>";
		}
		String typ = n.attrTyp().getFullName();
		for (String s : WurstConstants.JASSTYPES) {
			if ( s.equals(typ) ) {
				typ = "<font color=\"rgb(34,136,143)\">" + typ + " </font>";
				break;
			}
		}
		additionalProposalInfo += "<pre><hr />" + typ + n.getName()
					+ "<br /></pre>" + "defined in " + nearestScopeName(n);

		return new CompletionProposal(replacementString, replacementOffset, replacementLength,
				cursorPosition, image, displayString, contextInformation, additionalProposalInfo);
	}


	private String nearestScopeName(NameDef n) {
		if (n.attrNearestNamedScope() != null) {
			return Utils.printElement(n.attrNearestNamedScope());
		} else {
			return "Global";
		}
	}


	private ICompletionProposal makeFunctionCompletion(FunctionDefinition f) {
		String replacementString = f.getName() + "()";
		
		int replacementOffset = offset - alreadyEntered.length();
		int replacementLength = alreadyEntered.length();
		int cursorPosition = replacementString.length() - 1; // inside parentheses
		if (f.getParameters().size() == 0) {
			cursorPosition++; // outside parentheses
		}
		Image image = Icons.function;
		String comment = f.attrComment();
		comment = comment.replaceAll("\n", "<br />");
		StringBuilder descr = new StringBuilder();
		StringBuilder descrhtml = new StringBuilder();
		for (WParameter p : f.getParameters()) {
			if (descr.length() > 0) {
				descr.append(", ");
				descrhtml.append(", ");
			}
			String typ = p.attrTyp().toString();
			for (String s : WurstConstants.JASSTYPES) {
				if ( s.equals(typ) ) {
					typ = "<font color=\"rgb(34,136,143)\">" + typ + "</font>";
					break;
				}
			}
			descr.append(p.attrTyp() + " " + p.getName());
			descrhtml.append(typ + " " + p.getName());
		}
		String returnType = f.getReturnTyp().attrTyp().getFullName();
		String returnTypeHtml = returnType;
		for (String s : WurstConstants.JASSTYPES) {
			if ( s.equals(returnTypeHtml) ) {
				returnTypeHtml = "<font color=\"rgb(34,136,143)\">" + returnTypeHtml + "</font>";
				break;
			}
		}
		String displayString = f.getName() +"(" + descr.toString() + ") returns " + returnType + " - [" + nearestScopeName(f) +"]";
		IContextInformation contextInformation = descr.length() == 0 ? null : new ContextInformation(f.getName(), descr.toString());
		String additionalProposalInfo;
		if (f.attrComment().length() > 1) {
			additionalProposalInfo = comment;
		}else{
			additionalProposalInfo = "<i>No hotdoc provided</i>";
		}
		additionalProposalInfo += "<pre><hr /><b><font color=\"rgb(127,0,85)\">" + "function</font></b> " + f.getName() +"(" + descrhtml.toString() + ") "
				+ "<br /><b><font color=\"rgb(127,0,85)\">returns</font></b> " + returnTypeHtml
				+ "<br /></pre>" + "defined in " + nearestScopeName(f);

		
		return new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo);
	}
	
	private ICompletionProposal makeConstructorCompletion(ClassDef c, ConstructorDef constr) {
		String replacementString = c.getName() + "()";
		
		int replacementOffset = offset - alreadyEntered.length();
		int replacementLength = alreadyEntered.length();
		int cursorPosition = replacementString.length() - 1; // inside parentheses
		if (constr.getParameters().size() == 0) {
			cursorPosition++; // outside parentheses
		}
		Image image = Icons.block;
		String comment = constr.attrComment();
		comment = comment.replaceAll("\n", "<br />");
		StringBuilder descr = new StringBuilder();
		StringBuilder descrhtml = new StringBuilder();
		for (WParameter p : constr.getParameters()) {
			if (descr.length() > 0) {
				descr.append(", ");
				descrhtml.append(", ");
			}
			String typ = p.attrTyp().toString();
			for (String s : WurstConstants.JASSTYPES) {
				if ( s.equals(typ) ) {
					typ = "<font color=\"rgb(34,136,143)\">" + typ + "</font>";
					break;
				}
			}
			descr.append(p.attrTyp() + " " + p.getName());
			descrhtml.append(typ + " " + p.getName());
		}
		String displayString = c.getName() +"(" + descr.toString() + ")";
		IContextInformation contextInformation = descr.length() == 0 ? null : new ContextInformation(c.getName(), descr.toString());
		String additionalProposalInfo;
		if (constr.attrComment().length() > 1) {
			additionalProposalInfo = comment;
		}else{
			additionalProposalInfo = "<i>No hotdoc provided</i>";
		}
		additionalProposalInfo += "<pre><hr /><b><font color=\"rgb(127,0,85)\">" + "construct</font></b>(" + descrhtml.toString() + ") "
				+ "<br /></pre>" + "defined in class " + c.getName();

		
		return new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo);
	}


	private void completionsAddVisibleExtensionFunctions(String alreadyEntered,
			List<ICompletionProposal> completions,
			Multimap<String, NameLink> visibleNames, WurstType leftType) {
		for (Entry<String, NameLink> e : visibleNames.entries()) {
			if (!e.getKey().toLowerCase().startsWith(alreadyEntered)) {
				continue;
			}
			if (e.getValue().getNameDef() instanceof ExtensionFuncDef) {
				ExtensionFuncDef ef = (ExtensionFuncDef) e.getValue().getNameDef();
				if (ef.getExtendedType().attrTyp().isSupertypeOf(leftType, ef)) {
					completions.add(makeFunctionCompletion(ef));
				}
			}
		}
		
	}
	

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] {};
	}
	
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return validator;
	}

}
