package step2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.lang.*;

import step2.InvocationGraph;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import java.util.Scanner;

public class Parser {
	
	public static final String projectPath2 = "C:\\Users\\HP\\Desktop\\Automaton\\src";
	public static final String projectPath = "C:\\Users\\HP\\Downloads\\CorrectionTP2_Partie1 (1)\\CorrectionTP1_Partie1\\step2\\src";
	public static final String jrePath = "C:\\Program Files\\Java\\jdk-11.0.1\\lib\\jrt-fs.jar";
	
	public static void main(String[] args) throws IOException {
		
		
		
		System.out.println("Entrez le chemin vers l'SRC de votre projet JAVA: ");
		Scanner path_scanner= new Scanner(System.in);
		String project_path= path_scanner.nextLine();
		System.out.println("Entrez le chemin vers le jre: ");
		Scanner jre_scanner= new Scanner(System.in);
		String jre_path= jre_scanner.nextLine();
		// read java files
		final File folder = new File(project_path);
		
		//List all files of the project
		ArrayList<File> javaFiles = listJavaFilesForFolder(folder);
		
		//List of the packages of the project
		HashSet<String> packages= new HashSet<String>();
		//List of the classes of the project
		HashSet<TypeDeclaration> classes= new HashSet<TypeDeclaration>();
		//List of the methods of the project
		HashSet<MethodDeclaration> methods= new HashSet<MethodDeclaration>();
		//Map of classes and the number of their methods
		LinkedHashMap<Name, Integer> mpclass = new LinkedHashMap<>();
		//Map of classes and the number of their attributes
		LinkedHashMap<Name, Integer> fpclass = new LinkedHashMap<>();
		//Map of methods and the number of their parameters
		LinkedHashMap<Name, Integer> ppmethod = new LinkedHashMap<>();
		//Map of the methods and the number of their lines of code
		LinkedHashMap<Name, Integer> linespmethod = new LinkedHashMap<>();
		//List of lines of code per java file
		ArrayList<Integer> fileLines= new ArrayList<>();
		
		InvocationGraph invocG= new InvocationGraph();
		
		
		for (int index=0; index<javaFiles.size();index++) {
			
			File fileEntry = javaFiles.get(index); 
			
			//Get java file content
			String content = FileUtils.readFileToString(fileEntry);
			
			//Add the number of lines of code in the list
			fileLines.add(countLines(content));
			
			
			CompilationUnit parse = parse(content.toCharArray(), project_path,jre_path);

			//Collect the information of packages/classes/methods of the file
			collectPackageInfo(parse,packages);
			collectTypeDeclarationInfo(parse,classes);
			collectMethodInfo(parse,methods);
			collectMethodInvocationInfo(parse,invocG);
			
		}
		
		//invocG.printGraph();
		//System.out.println(invocG.graphDot());
		
		
		for (TypeDeclaration clss : classes) {
			//For each class, add the number of methods and attributes to the according maps
			
			mpclass.put(clss.getName(), clss.getMethods().length);
			fpclass.put(clss.getName(), clss.getFields().length);
		}
				
		for(MethodDeclaration m: methods) {
			//For each method, we add the number of parameters and lines of code to the according maps
			ppmethod.put(m.getName(), m.parameters().size());
			linespmethod.put(m.getName(), countLines(m.toString()));
		}
				
		//Sort the maps by value by a descending type to facilitate information extraction
		LinkedHashMap<Name, Integer> sppmethod = sortByValue(ppmethod);
		LinkedHashMap<Name, Integer> smpclass = sortByValue(mpclass);
		LinkedHashMap<Name, Integer> sfpclass = sortByValue(fpclass);
		LinkedHashMap<Name, Integer> slinespmethod = sortByValue(linespmethod);
		
		//Extract the top 10% classes by number of methods and attributes
		ArrayList<Name> topclassM = topPercentKeys(smpclass,10);
		ArrayList<Name> topclassF = topPercentKeys(sfpclass,10);
		
		//Get the intersection of the previous lists
		ArrayList<Name>  topclassBoth= new ArrayList<>();
		for (Name n:topclassM) {
			if(topclassF.contains(n)) {
				topclassBoth.add(n);
			}
		}
		
		String menu= "*********** Menu Principal de l'application ***************\n1- Nombre de classes de l'application \n2- Nombre de lignes de code de l’application \n3- Nombre total de méthodes de l’application \n4- Nombre total de packages de l’application \n5- Nombre moyen de méthodes par classe \n6- Nombre moyen de lignes de code par méthode \n7- Nombre moyen d'attribut par classe \n8- Les	10% des classes qui possèdent le plus grand nombre de méthodes \n9- Les	10% des classes qui possèdent le plus grand nombre d'attributs \n10- Les classes qui font partie en même temps des deux catégories précédentes \n11- Les classes qui possèdent plus de X méthodes \n12- Les	10% des méthodes qui possèdent le plus grand nombre de lignes de code \n13- Le nombre maximal de paramètres par rapport à toutes les méthodes de l'application \n14- Construire le graphe d'appel de l'application \n15- Quitter";
		System.out.println(menu);
		Boolean out=false;
		while(!out) {
			
	        System.out.print("Choisissez l'opération que vous voulez faire: ");
	        Scanner sc= new Scanner(System.in);
	        int in=sc.nextInt();
	        
			
			switch(in) {
				case(1):
					System.out.println("Nombre de classes du projet :"+ classes.size());
					out=false;
					break;
				case(2):
					System.out.println("Nombre de lignes de code du projet: "+ projectLines(fileLines));
					out=false;
					break;
				case(3):
					System.out.println("Nombre de méthodes du projet :"+ methods.size());
					out=false;
					break;
				case(4):
					System.out.println("Nombre de packages du projet :"+ packages.size());
					out=false;
					break;
				case(5):
					System.out.println("Moyenne de méthodes par classe est: "+ mapAverage(smpclass));
					out=false;
					break;
				case(6):
					System.out.println("Moyenne de lignes par méthode et: "+ mapAverage(slinespmethod));
					out=false;
					break;
				case(7):
					System.out.println("Moyenne d'attributs par classe est: "+ mapAverage(sfpclass));
					out=false;
					break;
				case(8):
					System.out.println("Les 10% top classes par méthodes sont: "+ topclassM);
					out=false;
					break;
				case(9):
					System.out.println("Les 10% top classes par attributs sont: "+ topclassF);
					out=false;
					break;
				case(10):
					System.out.println("Les classes des deux catégries: "+ topclassBoth);
					out=false;
					break;
				case(11):
					System.out.print("Entrez le nombre X de méthodes: ");
					Scanner sc2= new Scanner(System.in);
					int X=sc.nextInt();
					System.out.println("Les classes ayant plus de X méthodes: "+ keysAboveX(smpclass,X));
					out=false;
					break;
				case(12):
					System.out.println("Top 10% de méthodes par nombre de lignes: "+ topPercentKeys(slinespmethod,10));
					out=false;
					break;
				case(13):
					System.out.println("Nombre max d'attributs est: "+ (new ArrayList<Integer>(sppmethod.values())).get(0));
					out=false;
					break;	
				case(14):
					invocG.writeGraphInDotFile(project_path +"graph.dot");
					invocG.convertDotToSVG(project_path +"graph.dot");
					System.out.println("Graphe d'appel construit est sauvegarder dans un fichier .SVG dans le dossier du projet!");
					out=false;
					break;
				case(15):
				default:
					sc.close();
					out=true;
					
			}
			
		}
		
	}

	// read all java files from specific folder
	public static ArrayList<File> listJavaFilesForFolder(final File folder) {
		ArrayList<File> javaFiles = new ArrayList<File>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				javaFiles.addAll(listJavaFilesForFolder(fileEntry));
			} else if (fileEntry.getName().contains(".java")) {
				// System.out.println(fileEntry.getName());
				javaFiles.add(fileEntry);
			}
		}

		return javaFiles;
	}

	// create AST
	private static CompilationUnit parse(char[] classSource, String project_Path, String jre_Path) {
		ASTParser parser = ASTParser.newParser(AST.JLS4); // java +1.6
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 
		parser.setBindingsRecovery(true);
 
		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
 
		parser.setUnitName("");
 
		String[] sources = { project_Path }; 
		String[] classpath = {jre_Path};
 
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		parser.setSource(classSource);
		
		return (CompilationUnit) parser.createAST(null); // create and parse
	}

	// navigate method information
	public static void collectMethodInfo(CompilationUnit parse, HashSet<MethodDeclaration> methods ) {
		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		parse.accept(visitor);

		for (MethodDeclaration method : visitor.getMethods()) {
			methods.add(method);
			
			//System.out.println("Method name: " + method.getName()
				//	+ " Return type: " + method.getReturnType2());
		}

	}

	
	// navigate method invocations inside method
		public static void collectMethodInvocationInfo(CompilationUnit parse, InvocationGraph invocG) {
			
			
			MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
			parse.accept(visitor1);
			for (MethodDeclaration method : visitor1.getMethods()) {
				
				
				MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
				method.accept(visitor2);
				
				//TypeDeclaration method_class = (TypeDeclaration) method.getParent();
				
				if(visitor2.getMethods().size()!=0) {
				
					for (MethodInvocation methodInvocation : visitor2.getMethods()) {
						if(methodInvocation.getExpression()!=null || true) {
							if(methodInvocation.resolveMethodBinding()!=null) {
								/*System.out.print("method " + method.getName() + " invoc method "
										+ methodInvocation.getName());
								System.out.println(" calsse invoker: "+ methodInvocation.resolveMethodBinding().getDeclaringClass().getName());*/
								
								String key= method.resolveBinding().getDeclaringClass().getName().toString()+"::"+method.getName().toString();
								String value= methodInvocation.resolveMethodBinding().getDeclaringClass().getName().toString()+"::"+ methodInvocation.getName().toString(); 
								invocG.addInvoc(key,value );
							}
						}
						
					}
				}
				
			}
			
		}
		
		// navigate package information
		public static void collectPackageInfo(CompilationUnit parse, HashSet<String> packages) {
			PackageDeclarationVisitor visitor = new PackageDeclarationVisitor();
			parse.accept(visitor);

			for (PackageDeclaration method : visitor.getPackages()) {
				packages.add(method.getName().getFullyQualifiedName());
			}

		}	
		
		// navigate class information
		public static void collectTypeDeclarationInfo(CompilationUnit parse, HashSet<TypeDeclaration> classes) {
			TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
			parse.accept(visitor);

			for (TypeDeclaration t : visitor.getTypes()) {
				if(!t.isInterface()) {
				classes.add(t);}
			}

		}	
		
		
		// function to sort LinkedHashMap by values
	    public static LinkedHashMap<Name, Integer> sortByValue(LinkedHashMap<Name, Integer> hm)
	    {
	        // Create a list from elements of LinkedHashMap
	        List<Map.Entry<Name, Integer> > list =
	               new LinkedList<Map.Entry<Name, Integer> >(hm.entrySet());
	 
	        // Sort the list
	        Collections.sort(list, new Comparator<Map.Entry<Name, Integer> >() {
	            public int compare(Map.Entry<Name, Integer> o1,
	                               Map.Entry<Name, Integer> o2)
	            {
	                return (-1)*(o1.getValue()).compareTo(o2.getValue());
	            }
	        });
	         
	        // put data from sorted list to LinkedHashMap
	        LinkedHashMap<Name, Integer> temp = new LinkedHashMap<Name, Integer>();
	        for (Map.Entry<Name, Integer> aa : list) {
	            temp.put(aa.getKey(), aa.getValue());
	        }
	        return temp;
	    }
	    
	    private static int countLines(String str){
	    	   String[] lines = str.split("\r\n|\r|\n");
	    	   return  lines.length;
	    	}
	    
	    
	    public static int projectLines(ArrayList<Integer> fileLines) {
	    	int total=0;
	    	
	    	for(int x : fileLines) {
	    		total+=x;
	    	}
	    	return total;
	    }
	    
	    //Gets the average of map integer values
	    public static int mapAverage(LinkedHashMap<Name,Integer> map) {
	    	int total=0; 
	    	
	    	for(Map.Entry<Name,Integer> entry : map.entrySet()) {
	    		
	    		total+= entry.getValue();
	    	}
	    	
	    	return (total/map.size());
	    }
	    
	    //get top percentage of map Keys by sorted values
	    public static ArrayList<Name> topPercentKeys(LinkedHashMap<Name,Integer> map, int percentage){

	    	ArrayList<Name> topP = new ArrayList<>();
	    	int index = (map.size()*percentage)/100;
	    	ArrayList<Name> names= new ArrayList<Name>(map.keySet()) ;
	    
	    	for (int i=0; i<index; i++) {
	    		topP.add(names.get(i));
	    	}
	    	return topP;
	    
	    }
	    
	    //Get number of keys that have a value above X
	    public static ArrayList<Name> keysAboveX( LinkedHashMap<Name,Integer> map, int x){
	    	
	    	ArrayList<Name> topclasses= new ArrayList<>();
	    	for(Map.Entry<Name,Integer> entry : map.entrySet()) {
	    		
	    		if(entry.getValue()>= x) {
	    			topclasses.add(entry.getKey());
	    		}
	    	}
	    	return topclasses;
	    }
	    
	    
}