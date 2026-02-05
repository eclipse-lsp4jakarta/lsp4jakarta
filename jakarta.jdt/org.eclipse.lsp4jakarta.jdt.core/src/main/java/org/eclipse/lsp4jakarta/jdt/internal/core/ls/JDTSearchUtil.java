package org.eclipse.lsp4jakarta.jdt.internal.core.ls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class JDTSearchUtil {

	public static List<IJavaElement> getAllProjectReference(IType type) throws CoreException {
		List<IJavaElement> references = new ArrayList<IJavaElement>();
		// ===== DEBUGGING: Verify inputs =====
	    System.out.println("=== Search Debug Info ===");
	    System.out.println("Type: " + type.getFullyQualifiedName());
	    System.out.println("Type exists: " + type.exists());
	    System.out.println("Type is binary: " + type.isBinary());
	    System.out.println("Type project: " + type.getJavaProject().getElementName());
	    
		IJavaProject project = type.getJavaProject();
		System.out.println("Project exists: " + project.exists());
	    System.out.println("Project is open: " + project.getProject().isOpen());
	    
		SearchPattern pattern = SearchPattern.createPattern(
				type,
				IJavaSearchConstants.REFERENCES,
				SearchPattern.R_EXACT_MATCH);

		System.out.println("Pattern created: " + pattern);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
				new IJavaElement[] { project }, // Only this project
				IJavaSearchScope.SOURCES // Only source files (no JARs)
		);
		System.out.println("Scope elements: " + scope.enclosingProjectsAndJars().length);
	    for (IPath path : scope.enclosingProjectsAndJars()) {
	        System.out.println("  Scope includes: " + path);
	    }

		SearchRequestor requestor = new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				System.out.println(">>> Match found!");
	            System.out.println("    Element type: " + match.getElement().getClass().getName());
	            System.out.println("    Element: " + match.getElement());
	            
				if (match.getElement() instanceof IJavaElement) {
					IJavaElement element = (IJavaElement) match.getElement();
	                System.out.println("    Java element type: " + element.getElementType());
	                System.out.println("    Java element name: " + element.getElementName());
	                references.add(element);
					//references.add((IJavaElement) match.getElement());
				}
			}

		};

		SearchEngine searchEngine = new SearchEngine();
		System.out.println("\nStarting search...");
		searchEngine.search(
				pattern,
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				scope, // Project-only scope
				requestor,
				new NullProgressMonitor());
		System.out.println("Search complete. Found " + references.size() + " references.\n");
	    
		return references;
	}
}
