package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

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
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.AttrExprType;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Utils;

public class WurstCompletionProcessor implements IContentAssistProcessor {

	private static final int MAX_COMPLETIONS = 50;
	private final WurstEditor editor;
	private int offset;
	private String errorMessage;
	private IContextInformationValidator validator;
	private String alreadyEntered;
	private String alreadyEnteredLower;
	private int lastStartPos = -1;

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
		if (isEnteringRealNumber(viewer, offset)) {
			return null;
		}
		
		
		alreadyEntered = getAlreadyEnteredText(viewer, offset);
		alreadyEnteredLower = alreadyEntered.toLowerCase();
		System.out.println("already entered = " + alreadyEntered);
		
		int startPos = offset - alreadyEntered.length();
		
		CompilationUnit cu = null;
		System.out.println("POS " + startPos + " == " + lastStartPos);
		if (startPos == lastStartPos) {
			// we have already parsed the document, just use the old compilation unit
			cu = editor.getCompilationUnit();
		}
		lastStartPos = startPos;
		if (cu == null) {
			cu = editor.reconcile(false);
		}
		if (Utils.isEmptyCU(cu)) {
			errorMessage = "Could not parse file.";
			System.out.println("cu is empty ...");
			return null;
		}
		
		
		List<WurstCompletion> completions = Lists.newArrayList();
		
		
		
		AstElement elem =  Utils.getAstElementAtPos(cu, lastStartPos);
		System.out.println("get completions at " + Utils.printElement(elem));
		WurstType leftType = null;
		if (elem instanceof ExprMemberVar) {
			ExprMemberVar e = (ExprMemberVar) elem;
			leftType = e.getLeft().attrTyp();
			if (leftType instanceof WurstTypeNamedScope) {
				WurstTypeNamedScope ns = (WurstTypeNamedScope) leftType;
				Multimap<String, NameLink> visibleNames = ns.getDef().attrNameLinks();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, elem);
			} else if (leftType instanceof WurstTypeTuple) {
				WurstTypeTuple tt = (WurstTypeTuple) leftType;
				Multimap<String, NameLink> visibleNames = tt.getTupleDef().attrNameLinks();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, elem);
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
				if (isSuitableCompletion(p.getName())) {
					completions.add(makeNameDefCompletion(p));
				}
			}
		} else if (elem instanceof ExprNewObject) {
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
				for (NameLink n : visibleNames.values()) {
					if (n.getNameDef() instanceof ClassDef 
						&& isSuitableCompletion(n.getName())) {
						ClassDef c = (ClassDef) n.getNameDef();
						for (ConstructorDef constr : c.attrConstructors()) {
							completions.add(makeConstructorCompletion(c, constr));
						}
					}
				}
				scope = scope.attrNextScope();
			}
		} else {
			leftType = AttrExprType.caclulateThistype(elem, false);
			if (leftType instanceof WurstTypeUnknown) {
				leftType = null;
			}
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, elem);
				scope = scope.attrNextScope();
			}
		}
		removeDuplicates(completions);
		
//		Collections.sort(completions, c)
		
		if (completions.size() > 0) {
			return toCompletionsArray(completions);
		}
		errorMessage = null;
		return null;
	}


	private ICompletionProposal[] toCompletionsArray(
			List<WurstCompletion> completions) {
		Collections.sort(completions);
		ICompletionProposal[] result = new ICompletionProposal[completions.size()];
		for (int i=0; i<result.length; i++) {
			result[i] = completions.get(i).getProposal();
		}
		return result;
	}


	public boolean isSuitableCompletion(String name) {
//		return name.toLowerCase().startsWith(alreadyEntered);
		boolean r = Utils.isSubsequence(alreadyEntered, name);
//		System.out.println("isSuitable? " + name + " for " + alreadyEntered + " -> " + r);
		return r;
	}


	


	private boolean isEnteringRealNumber(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		try {
			String before = doc.get(offset-2, 1);
			if (before.matches("[0-9]")) {
				// we are entering a real
				return true;
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	


	private void removeDuplicates(List<WurstCompletion> completions) {
		for (int i=0; i<completions.size()-1; i++) {
			String displayStringI = firstPartOfDisplayString(completions.get(i).getDisplayString());
			for (int j=completions.size()-1; j>i; j--) {				
				String displayStringJ =firstPartOfDisplayString(completions.get(j).getDisplayString());
				if (displayStringI.equals(displayStringJ)) {
					completions.remove(j);
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
			return doc.get(start, offset-start);
		} catch (BadLocationException e) {
		}
		return "";
	}


	private void completionsAddVisibleNames(String alreadyEntered,
			List<WurstCompletion> completions, Multimap<String, NameLink> visibleNames, WurstType leftType, AstElement pos) {
		for (Entry<String, NameLink> e : visibleNames.entries()) {
			if (completions.size() >= MAX_COMPLETIONS) {
				return;
			}
			if (!isSuitableCompletion(e.getKey())) {
				continue;
			}
//			System.out.println("aadasdas " + e.getValue());
			WurstType receiverType = e.getValue().getReceiverType();
			if (leftType == null) {
				if (receiverType != null) { 
					// skip extension functions, when not needed 
					continue;
				}
			} else {
//				if (receiverType instanceof WurstTypeNamedScope) {
//					WurstTypeNamedScope rt = (WurstTypeNamedScope) receiverType;
//					receiverType = rt.setTypeArgs(leftType.getTypeArgBinding());
//				}
				if (!leftType.isSubtypeOf(receiverType, pos)) {
					// skip elements with wrong receiver type
					System.out.println("	receiver = " + receiverType);
					continue;
				}
			}
			if (e.getValue().getNameDef() instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) e.getValue().getNameDef();
				
				WurstCompletion completion = makeFunctionCompletion(f);
				completions.add(completion);
			} else {
				completions.add(makeNameDefCompletion(e.getValue().getNameDef()));
			}
			
		}
	}
	
	private WurstCompletion makeNameDefCompletion(NameDef n) {
		String replacementString = n.getName();
		int replacementOffset = offset - alreadyEntered.length();
		int replacementLength = alreadyEntered.length();
		int cursorPosition = replacementString.length();
		Image image = Icons.var;
		RGB rbg  = new RGB(255, 255, 222);
		
		String comment = n.attrComment();
		comment = comment.replaceAll("\n", "<br />");
		
		String displayString = n.getName() + " : " + n.attrTyp().toString() + " - [" + nearestScopeName(n) +"]";
		IContextInformation contextInformation= new ContextInformation(
				n.getName(), Utils.printElement(n)+" : " + n.attrTyp().getFullName() + " -  defined in " + nearestScopeName(n)); //$NON-NLS-1$
		String additionalProposalInfo;
		if (n.attrComment().length() > 1) {
			additionalProposalInfo = comment;
		}else{
			additionalProposalInfo = "<i>No hotdoc provided</i>";
		}
		additionalProposalInfo += "<pre><hr />" + htmlType(n.attrTyp()) + n.getName()
					+ "<br /></pre>" + "defined in " + nearestScopeName(n);

		double rating = calculateRating(n.getName());
		return new WurstCompletion(replacementString, replacementOffset, replacementLength,
				cursorPosition, image, displayString, contextInformation, additionalProposalInfo, rating);
	}


	private double calculateRating(String name) {
		if (name.startsWith(alreadyEntered)) {
			// perfect match
			return 1;
		}
		String nameLower = name.toLowerCase();
		if (nameLower.startsWith(alreadyEnteredLower)) {
			// close to perfect
			return 0.999;
		}
		if (alreadyEntered.isEmpty()) {
			return 0.5;
		}
		return Math.max(
				Utils.averageSubsequenceLength(alreadyEntered, name),
				Utils.averageSubsequenceLength(alreadyEnteredLower, nameLower)
				) / (alreadyEntered.length()+1);
	}


	private String nearestScopeName(NameDef n) {
		if (n.attrNearestNamedScope() != null) {
			return Utils.printElement(n.attrNearestNamedScope());
		} else {
			return "Global";
		}
	}


	private WurstCompletion makeFunctionCompletion(FunctionDefinition f) {
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
			descr.append(p.attrTyp() + " " + p.getName());
			descrhtml.append(htmlType(p.attrTyp()) + " " + p.getName());
		}
		String returnType = f.getReturnTyp().attrTyp().toString();
		String returnTypeHtml = htmlType(f.getReturnTyp().attrTyp());
		String displayString = f.getName() +"(" + descr.toString() + ") returns " + returnType + " - [" + nearestScopeName(f) +"]";
		IContextInformation contextInformation = descr.length() == 0 ? null : new ContextInformation(f.getName(), descr.toString());
		String additionalProposalInfo;
		if (f.attrComment().length() > 1) {
			additionalProposalInfo = comment;
		}else{
			additionalProposalInfo = "<i>No hotdoc provided</i>";
		}
		
		String funcName = f.getName();
		if (f instanceof ExtensionFuncDef) {
			ExtensionFuncDef exf = (ExtensionFuncDef) f;
			funcName = htmlType(exf.getExtendedType().attrTyp()) + "." + funcName;
		}
		additionalProposalInfo += "<pre><hr /><b><font color=\"rgb(127,0,85)\">" + "function</font></b> " + funcName +"(" + descrhtml.toString() + ") "
				+ "<br /><b><font color=\"rgb(127,0,85)\">returns</font></b> " + returnTypeHtml
				+ "<br /></pre>" + "defined in " + nearestScopeName(f);

		
		double rating = calculateRating(f.getName());
		return new WurstCompletion(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo, rating);
	}
	
	private WurstCompletion makeConstructorCompletion(ClassDef c, ConstructorDef constr) {
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
			descr.append(p.attrTyp() + " " + p.getName());
			descrhtml.append(htmlType(p.attrTyp()) + " " + p.getName());
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

		double rating = calculateRating(c.getName());
		return new WurstCompletion(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo, rating);
	}


	public String htmlType(WurstType attrTyp) {
		String typ = attrTyp.toString();
		for (String s : WurstConstants.JASSTYPES) {
			if ( s.equals(typ) ) {
				return "<font color=\"rgb(34,136,143)\">" + typ + "</font>";
			}
		}
		typ = typ.replace("<", "&lt;");
		typ = typ.replace(">", "&gt;");
		return typ;
	}


	private void completionsAddVisibleExtensionFunctions(String alreadyEntered,
			List<WurstCompletion> completions,
			Multimap<String, NameLink> visibleNames, WurstType leftType) {
		for (Entry<String, NameLink> e : visibleNames.entries()) {
			if (!isSuitableCompletion(e.getKey())) {
				continue;
			}
			if (e.getValue().getNameDef() instanceof ExtensionFuncDef) {
				ExtensionFuncDef ef = (ExtensionFuncDef) e.getValue().getNameDef();
				if (ef.getExtendedType().attrTyp().dynamic().isSupertypeOf(leftType, ef)) {
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
