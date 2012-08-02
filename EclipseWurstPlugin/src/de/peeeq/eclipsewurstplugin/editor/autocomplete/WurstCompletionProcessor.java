package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.eclipsewurstplugin.editor.outline.Icons;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
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
		CompilationUnit cu = editor.reconcile();
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
			PscriptType leftType = e.getLeft().attrTyp();
			if (leftType instanceof PscriptTypeNamedScope) {
				PscriptTypeNamedScope ns = (PscriptTypeNamedScope) leftType;
				Multimap<String, NameDef> visibleNames = ns.getDef().attrVisibleNamesPrivate();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
			}
			
			// add member vars
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameDef> visibleNames = scope.attrVisibleNamesPrivate();
				
				completionsAddVisibleExtensionFunctions(alreadyEntered, completions, visibleNames, leftType);
				scope = scope.attrNextScope();
			}
		} else if (elem instanceof ExprRealVal) {
			// show no hints for reals
			return null;
		} else {
			WScope scope = elem.attrNearestScope();
			while (scope != null) {
				Multimap<String, NameDef> visibleNames = scope.attrVisibleNamesPrivate();
				completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
				scope = scope.attrNextScope();
			}
		}
		if (completions.size() > 0) {
			return completions.toArray(new ICompletionProposal[completions.size()]);
		}
		errorMessage = null;
		return null;
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
			List<ICompletionProposal> completions, Multimap<String, NameDef> visibleNames) {
		for (Entry<String, NameDef> e : visibleNames.entries()) {
			if (!e.getKey().startsWith(alreadyEntered)) {
				continue;
			}
			if (e.getValue() instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) e.getValue();
				
				ICompletionProposal completion = makeFunctionCompletion(f);
				completions.add(completion);
			} else {
				completions.add(makeNameDefCompletion(e.getValue()));
			}
			
		}
	}
	
	private ICompletionProposal makeNameDefCompletion(NameDef n) {
		String replacementString = n.getName().substring(alreadyEntered.length());
		int replacementOffset = offset;
		int replacementLength = 0;
		int cursorPosition = replacementString.length();
		Image image = Icons.var;
		String displayString = n.getName() + " : " + n.attrTyp().getFullName();
		IContextInformation contextInformation= new ContextInformation(
				n.getName(), Utils.printElement(n)); //$NON-NLS-1$
		String additionalProposalInfo = ":-)";
		return new CompletionProposal(replacementString, replacementOffset, replacementLength,
				cursorPosition, image, displayString, contextInformation, additionalProposalInfo);
	}


	private ICompletionProposal makeFunctionCompletion(FunctionDefinition f) {
		String replacementString = f.getName() + "()";
		
		replacementString = replacementString.substring(alreadyEntered.length());
		int replacementOffset = offset;
		int replacementLength = 0;
		int cursorPosition = replacementString.length() - 1; // inside parentheses
		if (f.getParameters().size() == 0) {
			cursorPosition++; // outside parentheses
		}
		Image image = Icons.function;
		
		StringBuilder descr = new StringBuilder();
		for (WParameter p : f.getParameters()) {
			if (descr.length() > 0) {
				descr.append(", ");
			}
			descr.append(p.attrTyp() + " " + p.getName());
		}
		String returnType = f.getReturnTyp().attrTyp().getFullName();
		String displayString = f.getName() +"(" + descr.toString() + ") : " + returnType;
		IContextInformation contextInformation = descr.length() == 0 ? null : new ContextInformation(f.getName(), descr.toString());
		String additionalProposalInfo = ":-)";
		return new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo);
	}


	private void completionsAddVisibleExtensionFunctions(String alreadyEntered,
			List<ICompletionProposal> completions,
			Multimap<String, NameDef> visibleNames, PscriptType leftType) {
		for (Entry<String, NameDef> e : visibleNames.entries()) {
			if (!e.getKey().startsWith(alreadyEntered)) {
				continue;
			}
			if (e.getValue() instanceof ExtensionFuncDef) {
				ExtensionFuncDef ef = (ExtensionFuncDef) e.getValue();
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
