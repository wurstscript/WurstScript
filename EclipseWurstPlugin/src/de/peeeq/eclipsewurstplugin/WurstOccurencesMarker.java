package de.peeeq.eclipsewurstplugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.swt.widgets.Display;

import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;

/**
 * marks occurrences of selected elements
 */
public class WurstOccurencesMarker {

	private final String type = "de.peeeq.wurstscript.occurence";
	private volatile @Nullable UpdateTask currentTask;

	public synchronized void update(@Nullable ITextSelection currentSelection, IAnnotationModel annotationModel,
			@Nullable String fileName, ModelManager modelManager) {
		if (currentSelection == null) {
			return;
		}
		if (fileName == null) {
			return;
		}

		if (currentTask != null) {
			currentTask.cancelled = true;
		}
		UpdateTask task = new UpdateTask(currentSelection.getOffset(), annotationModel, fileName, modelManager);
		task.start();
	}

	private class UpdateTask extends Thread {

		private int textOffset;
		private IAnnotationModel annotationModel;
		volatile boolean cancelled = false;
		private ModelManager modelManager;
		private String fileName;

		private UpdateTask(int textOffset, IAnnotationModel annotationModel, String fileName,
				ModelManager modelManager) {
			this.textOffset = textOffset;
			this.annotationModel = annotationModel;
			this.fileName = fileName;
			this.modelManager = modelManager;
		}

		@Override
		public void run() {
			System.out.println("run ");

			if (cancelled) {
				System.out.println("cancel1");
				return;
			}

			NameDef[] defBox = { null };
			modelManager.doWithCompilationUnit(fileName, cu -> {
				AstElement elem;
				elem = Utils.getAstElementAtPos(cu, textOffset, false);
				// get the linked name def
				defBox[0] = elem.tryGetNameDef();
			});
			NameDef def = defBox[0];

			if (cancelled) {
				System.out.println("cancel2");
				return;
			}

			Map<Annotation, Position> newAnnotations = new LinkedHashMap<>();
			modelManager.doWithCompilationUnit(fileName, cu -> {

				if (def != null) {
					Deque<AstElement> todo = new ArrayDeque<>();
					todo.push(cu);
					while (!todo.isEmpty()) {
						AstElement e = todo.pop();
						if (cancelled) {
							System.out.println("cancel3");
							return;
						}
						// visit children:
						for (int i = 0; i < e.size(); i++) {
							todo.push(e.get(i));
						}
						NameDef e_def;
						synchronized (modelManager) {
							e_def = e.tryGetNameDef();
						}
						if (e_def == def) {
							Annotation annotation = new Annotation(type, false, "marking occurrence");
							WPos pos = e.attrErrorPos();
							int length = pos.getRightPos() - pos.getLeftPos();
							if (length > 0) {
								Position position = new Position(pos.getLeftPos(), length);
								newAnnotations.put(annotation, position);
							}
						}
					}
				}
			});

			if (cancelled) {
				System.out.println("cancel4");
				return;
			}

			// collect old mark-occurences:
			Iterator<?> it = annotationModel.getAnnotationIterator();
			List<Annotation> toRemove = new ArrayList<>();

			while (it.hasNext()) {
				Annotation a = (Annotation) it.next();
				if (a.getType().equals(type)) {
					toRemove.add(a);
				}
			}

			if (!(toRemove.isEmpty() && newAnnotations.isEmpty())) {
				Display.getDefault().asyncExec(() -> {
					// not sure if this is correct to call from this thread ...
					((IAnnotationModelExtension) annotationModel)
							.replaceAnnotations(toRemove.toArray(new Annotation[0]), newAnnotations);
				});
			}
		}
	}
}
