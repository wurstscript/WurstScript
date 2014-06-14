package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import java.util.Collection;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.eclipsewurstplugin.editor.outline.Icons;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprEmpty;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMember;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.AttrExprType;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
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
	private int lastdocumentHash = 0;
	private ITextViewer currentViewer;

	public WurstCompletionProcessor(WurstEditor editor) {
		this.editor = editor;
		this.validator = new WurstContextInformationValidator();
	}


	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {'.'};
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, final int offset) {
		this.offset = offset;
		this.currentViewer = viewer;
		if (isEnteringRealNumber(viewer, offset)) {
			return null;
		}


		alreadyEntered = getAlreadyEnteredText(viewer, offset);
		alreadyEnteredLower = alreadyEntered.toLowerCase();
		WLogger.info("already entered = " + alreadyEntered);

		int startPos = offset - alreadyEntered.length();

		CompilationUnit cu = null;
		if (startPos == lastStartPos 
				&& lastdocumentHash == editor.getLastReconcileDocumentHashcode()) {
			// we have already parsed the document, just use the old compilation unit
			cu = editor.getCompilationUnit();
		}
		lastStartPos = startPos;
		lastdocumentHash = viewer.getDocument().get().hashCode();
		if (cu == null) {
			cu = editor.reconcile(false);
		}
		if (Utils.isEmptyCU(cu)) {
			errorMessage = "Could not parse file.";
			return null;
		}


		List<WurstCompletion> completions = Lists.newArrayList();



		AstElement elem =  Utils.getAstElementAtPos(cu, lastStartPos, false);
		WLogger.info("get completions at " + Utils.printElement(elem));
		WurstType leftType = null;
		boolean isMemberAccess = false;
		do { // dummy loop for using break
			if (elem instanceof ExprMember) {
				ExprMember e = (ExprMember) elem;

				if (elem instanceof ExprMemberMethod) {
					ExprMemberMethod c = (ExprMemberMethod) elem;
					if (isInParenthesis(viewer, offset, c.getLeft().getSource().getRightPos())) {
						// cursor inside parenthesis
						getCompletionsForExistingMemberCall(offset, completions, c);
						break;
					}
				}

				leftType = e.getLeft().attrTyp();
				isMemberAccess = true;
				WScope scope = elem.attrNearestScope();
				// add member vars
				while (scope != null) {
					Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
					completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, isMemberAccess, elem);
					completionsAddVisibleExtensionFunctions(alreadyEntered, completions, visibleNames, leftType);
					scope = scope.attrNextScope();
				}
			} else if (elem instanceof ExprRealVal) {
				// show no hints for reals
			} else if (elem instanceof WPackage) {
				// no hints at package level
			} else if (elem instanceof WImport) {
				//WImport imp = (WImport) elem;
				WurstModel model = elem.getModel();
				for (WPackage p : model.attrPackagesFresh().values()) {
					if (isSuitableCompletion(p.getName())) {
						completions.add(makeNameDefCompletion(p));
					}
				}
			} else if (elem instanceof ExprNewObject) {
				ExprNewObject en = (ExprNewObject) elem;
				if (offset > en.getSource().getLeftPos() + 4 + en.getTypeName().length()) {
					// cursor inside parameters
					getCompletionsForExistingConstructorCall(offset, completions, en);
					break;
				}
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
			} else if (elem instanceof ExprFunctionCall) {
				ExprFunctionCall c = (ExprFunctionCall) elem;
				if (offset > c.getSource().getLeftPos() + c.getFuncName().length()) {
					// cursor is in parameter list
					getCompletionsForExistingCall(offset, completions, c);
				} else {
					addDefaultCompletions(completions, elem, isMemberAccess);
				}
			} else {
				if (elem instanceof ExprEmpty) {
					if (elem.getParent() instanceof Arguments) {
						if (elem.getParent().getParent() instanceof ExprFunctionCall) {
							ExprFunctionCall c = (ExprFunctionCall) elem.getParent().getParent();
							getCompletionsForExistingCall(offset, completions, c);
						} else if (elem.getParent().getParent() instanceof ExprMemberMethod) {
							ExprMemberMethod c = (ExprMemberMethod) elem.getParent().getParent();
							getCompletionsForExistingMemberCall(offset,
									completions, c);
						} else if (elem.getParent().getParent() instanceof ExprNewObject) {
							ExprNewObject c = (ExprNewObject) elem.getParent().getParent();
							getCompletionsForExistingConstructorCall(offset,
									completions, c);
						}
						// TODO add overloaded funcs
					}
				}
				if (completions.isEmpty()) {
					// default completions:
					addDefaultCompletions(completions, elem, isMemberAccess);
				}
			}
		} while (false);

		dropBadCompletions(completions);
		removeDuplicates(completions);

		//		Collections.sort(completions, c)

		if (completions.size() > 0) {
			return toCompletionsArray(completions);
		}
		errorMessage = null;
		return null;
	}


	private boolean isInParenthesis(ITextViewer viewer, int offset, int start) {
		try {
			String s = viewer.getDocument().get(start, offset-start);
			return s.contains("(");
		} catch (BadLocationException e) {
			return false;
		}
	}


	private void addDefaultCompletions(List<WurstCompletion> completions,
			AstElement elem, boolean isMemberAccess) {
		WurstType leftType;
		leftType = AttrExprType.caclulateThistype(elem, true, null);
		if (leftType instanceof WurstTypeUnknown) {
			leftType = null;
		}
		WScope scope = elem.attrNearestScope();
		while (scope != null) {
			Multimap<String, NameLink> visibleNames = scope.attrNameLinks();
			completionsAddVisibleNames(alreadyEntered, completions, visibleNames, leftType, isMemberAccess, elem);
			scope = scope.attrNextScope();
		}
	}


	private void getCompletionsForExistingConstructorCall(final int offset,
			List<WurstCompletion> completions, ExprNewObject c) {
		if (c.attrConstructorDef() != null) {
			completions.add(
					makeConstructorCompletion(c.attrConstructorDef().attrNearestClassDef(), c.attrConstructorDef())
					.withDisableAction(offset));
		}
	}


	private void getCompletionsForExistingMemberCall(final int offset,
			List<WurstCompletion> completions, ExprMemberMethod c) {
		if (c.attrFuncDef() != null) {
			completions.add(
					makeFunctionCompletion(c.attrFuncDef())
					.withDisableAction(offset));
		}
	}


	private void getCompletionsForExistingCall(final int offset,
			List<WurstCompletion> completions, ExprFunctionCall c) {
		if (c.attrFuncDef() != null) {
			alreadyEntered = c.getFuncName();
			completions.add(
					makeFunctionCompletion(c.attrFuncDef())
					.withDisableAction(offset));
		}
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


	private boolean isSuitableCompletion(String name) {
		if (alreadyEntered.length() <= 2) {
			return name.toLowerCase().startsWith(alreadyEnteredLower);
		} else if (alreadyEntered.length() <= 3) {
			return name.toLowerCase().contains(alreadyEnteredLower);
		} else {
			return Utils.isSubsequenceIgnoreCase(alreadyEntered, name);
		}
	}





	private boolean isEnteringRealNumber(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		try {
			String before = doc.get(offset-2, 2);
			if (before.matches("[0-9]\\.")) {
				// we are entering a real
				return true;
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	private boolean isBeforeParenthesis(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		try {
			String t = doc.get(offset, 1);
			if (t.equals("(")) {
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
			List<WurstCompletion> completions, Multimap<String, NameLink> visibleNames, WurstType leftType, boolean isMemberAccess, AstElement pos) {
		Collection<Entry<String, NameLink>> entries = visibleNames.entries();
		for (Entry<String, NameLink> e : entries) {
			if (!isSuitableCompletion(e.getKey())) {
				continue;
			}

			// remove invisible functions
			if (e.getValue().getVisibility() == Visibility.PRIVATE_OTHER
					|| e.getValue().getVisibility() == Visibility.PROTECTED_OTHER) {
				continue;
			}

			WurstType receiverType = e.getValue().getReceiverType();
			if (leftType == null) {
				if (receiverType != null) { 
					// skip extension functions, when not needed 
					continue;
				}
			} else { // leftType != null

				if (!leftType.isSubtypeOf(receiverType, pos)) {
					// skip elements with wrong receiver type
					if (!isMemberAccess && receiverType == null) {
						// if it is not a member access, then also include normals vars
						// and functions
					} else {
						// otherwise skip
						continue;
					}
				}
			}

			if (e.getValue().getNameDef() instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) e.getValue().getNameDef();

				WurstCompletion completion = makeFunctionCompletion(f);
				completions.add(completion);
			} else {
				completions.add(makeNameDefCompletion(e.getValue().getNameDef()));
			}
			if (alreadyEntered.length() <= 3 
					&& completions.size() >= MAX_COMPLETIONS) {
				// got enough completions
				return;
			}
		}
	}

	private void dropBadCompletions(List<WurstCompletion> completions) {
		Collections.sort(completions);
		for (int i=completions.size()-1; i>=MAX_COMPLETIONS; i--) {
			if (completions.get(i).getRating() > 0.4) {
				// good enough
				return;
			}
			completions.remove(i);
		}
	}


	private WurstCompletion makeNameDefCompletion(NameDef n) {
		String replacementString = n.getName();
		int replacementOffset = offset - alreadyEntered.length();
		int replacementLength = alreadyEntered.length();
		int cursorPosition = replacementString.length();
		Image image = Icons.var;

		String comment = n.attrComment();
		comment = comment.replaceAll("\n", "<br />");

		String displayString = n.getName() + " : " + n.attrTyp().toString() + " - [" + nearestScopeName(n) +"]";
		IContextInformation contextInformation= new ContextInformation(
				n.getName(), Utils.printElement(n)+" : " + n.attrTyp().getFullName() + " -  defined in " + nearestScopeName(n)); //$NON-NLS-1$

		String additionalProposalInfo2 = n.descriptionHtml();
		double rating = calculateRating(n.getName());
		return new WurstCompletion(replacementString, replacementOffset, replacementLength,
				cursorPosition, image, displayString, contextInformation, additionalProposalInfo2, rating);
	}


	private double calculateRating(String name) {
		if (alreadyEntered.isEmpty()) {
			return 0.5;
		}
		if (name.startsWith(alreadyEntered)) {
			// perfect match
			return 1.23;
		}
		String nameLower = name.toLowerCase();
		if (nameLower.startsWith(alreadyEnteredLower)) {
			// close to perfect
			return 0.999;
		}


		int ssLen;
		if (Utils.isSubsequence(alreadyEntered, name)) {
			ssLen = Math.min(
					Utils.subsequenceLengthes(alreadyEntered, name).size(),
					Utils.subsequenceLengthes(alreadyEnteredLower, nameLower).size());
		} else {
			ssLen = Utils.subsequenceLengthes(alreadyEnteredLower, nameLower).size();
		}
		return 1 - ssLen * 1. / alreadyEntered.length();
	}


	private static String nearestScopeName(NameDef n) {
		if (n.attrNearestNamedScope() != null) {
			return Utils.printElement(n.attrNearestNamedScope());
		} else {
			return "Global";
		}
	}


	private WurstCompletion makeFunctionCompletion(FunctionDefinition f) {
		String replacementString = f.getName();
		if (!isBeforeParenthesis(currentViewer, offset)) {
			replacementString += "()";
		}

		int replacementOffset = offset - alreadyEntered.length();
		int replacementLength = alreadyEntered.length();
		int cursorPosition = replacementString.length() - 1; // inside parentheses
		if (f.getParameters().size() == 0) {
			cursorPosition++; // outside parentheses
		}
		Image image = Icons.function;

		String displayString = getFunctionDescriptionShort(f);

		IContextInformation contextInformation = null;
		if (!f.getParameters().isEmpty()) {
			// context information for parameters
			contextInformation = new ContextInformation(f.getName(), Utils.getParameterListText(f));
		}


		String additionalProposalInfo2 = getFunctionDescriptionHtml(f);

		double rating = calculateRating(f.getName());
		return new WurstCompletion(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo2, rating);
	}


	private String getFunctionDescriptionShort(FunctionDefinition f) {
		String comment = f.attrComment();
		comment = comment.replaceAll("\n", "<br />");
		String returnType = f.getReturnTyp().attrTyp().toString();
		String displayString = f.getName() +"(" + Utils.getParameterListText(f) + ") returns " + returnType + " - [" + nearestScopeName(f) +"]";
		return displayString;
	}

	public static String getFunctionDescriptionHtml(FunctionDefinition f) {
		return f.descriptionHtml();
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

		String params = Utils.getParameterListText(constr);
		IContextInformation contextInformation = params.length() == 0 ? null : new ContextInformation(c.getName(), params);
		String displayString = c.getName() +"(" + Utils.getParameterListText(constr) + ")";

		String additionalProposalInfo2 = constr.descriptionHtml();

		double rating = calculateRating(c.getName());
		return new WurstCompletion(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo2, rating);
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
