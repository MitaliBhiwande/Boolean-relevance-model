package mitalivi_project2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class mitalivi_project2 {
	 static int counterDAATOr=0;						//counter for comparisons of DaatOr
	 static int counterDAATAnd=0;						//counter for comparisons of DaatAnd

	private static String inputfile;					//inputfile variable
	private static PrintWriter outputfile;				//outputfile variable
	
	private static void PrintingPostings(String query, HashMap<String, TreeSet<Integer>> Map) {			//displaying postings of terms

		String querySplit[] = query.split(" ");
		for (int queryterm = 0; queryterm < querySplit.length; queryterm++) {

			TreeSet<Integer> termtree = Map.get(querySplit[queryterm]);
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(termtree);

			String formattedlist = termpostings.toString().replace("[", "").replace(",", "").replace("]", "").trim(); //format the postings
			outputfile.write("GetPostings\n" + querySplit[queryterm] + "\nPostings list: " + formattedlist + "\n");
		}
	}

	public static void PrintingTaatOR(String query, ArrayList<Integer> termTAATOr, int size, int count) {		//displaying output for TaatOr
		String formattedlist;
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = termTAATOr.toString().replace("[", "").replace(",", "").replace("]", "").trim();	//format the results
		outputfile.println("TaatOr\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ termTAATOr.size() + "\nNumber of comparisons: " + count);
	}

	public static void PrintingTaatAND(String query, ArrayList<Integer> termTAATAnd, int size, int count) {			//displaying output for TaatAnd
		String formattedlist;
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = termTAATAnd.toString().replace("[", "").replace(",", "").replace("]", "").trim();		//format the results
		outputfile.println("TaatAnd\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ termTAATAnd.size() + "\nNumber of comparisons: " + count);
	}

	public static void PrintingDaatOr(String query, ArrayList<Integer> dAATOr, int size, int count) {			//Displaying output for DaatOr
		String formattedlist;
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = dAATOr.toString().replace("[", "").replace(",", "").replace("]", "").trim();	//format the result
		outputfile.println("DaatOr\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ size + "\nNumber of comparisons: " + count);

	}

	private static void PrintingDaatAnd(String query, ArrayList<Integer> dAATAnd, int size, int count) {		//Displaying output for DaatAnd
		String formattedlist;
		if (size == 0)
			formattedlist = "empty";
		else
			formattedlist = dAATAnd.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("DaatAnd\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ size + "\nNumber of comparisons: " + count);

	}

	public static void main(String args[]) throws Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(args[0]))); // gets the index file from args[0]
																							
		inputfile = args[2]; 															// gets input query file from args[2]
		BufferedReader br = new BufferedReader(new FileReader(mitalivi_project2.inputfile));
		String inputline = null;

		String outputpath = args[1]; 													// gets outputfile from args[1]
		outputfile = new PrintWriter(outputpath, "UTF-8");

		HashMap<String, TreeSet<Integer>> invertedIndex = new HashMap<String, TreeSet<Integer>>(); //hashmap where the postings of all terms are generated and stored
		ArrayList<String> languages = new ArrayList<String>();							//adds all language fields to be considered in the ArrayList
		languages.add("text_nl");
		languages.add("text_fr");
		languages.add("text_de");
		languages.add("text_ja");
		languages.add("text_ru");
		languages.add("text_pt");
		languages.add("text_es");
		languages.add("text_it");
		languages.add("text_da");
		languages.add("text_no");
		languages.add("text_sv");

		for (String lang : languages) {
			Terms terms = MultiFields.getTerms(reader, lang); 							// get all terms of this field
			TermsEnum termsenum = terms.iterator();
			BytesRef term = null;
			while ((term = termsenum.next()) != null) {
				PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, lang, term);		//gets all terms of the specific language from inputfile
				TreeSet<Integer> temp = new TreeSet<>();							//stores the postings
				while (postingsEnum.nextDoc() != PostingsEnum.NO_MORE_DOCS) {		//while there are more doc id's
					if (invertedIndex.containsKey(term.utf8ToString())) {			//if the hashmap contains the term's postings
						temp = invertedIndex.get(term.utf8ToString());				//add the particular postings of term to temp
						temp.add(postingsEnum.docID());								//add the above postings to treeset with docID

					} else {
						temp.add(postingsEnum.docID());
					}
					invertedIndex.put(term.utf8ToString(), temp);
				}
			}
		}
		while ((inputline = br.readLine()) != null) {						//till the inputfile contains queryterms on each line
			PrintingPostings(inputline, invertedIndex);						//display postings of query terms
			TAATAnd(inputline, invertedIndex);								//call to the respective functions
			TAATOr(inputline, invertedIndex);
			DAATAnd(inputline, invertedIndex);
			DAATOr(inputline, invertedIndex);
		}
		br.close();														//close the outputfile
		outputfile.close();
	}

	public static ArrayList<ArrayList> sortPostingList(String query, HashMap<String, TreeSet<Integer>> Map) { //sorting function to sort the postings of query terms as per their size
		ArrayList<ArrayList> postingArrayList = new ArrayList<ArrayList>();
		String querySplit[] = query.split(" ");
		for (int queryterm = 0; queryterm < querySplit.length; queryterm++) {
			TreeSet<Integer> termtree = Map.get(querySplit[queryterm]);
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(termtree);
			postingArrayList.add(termpostings);
		}
		Collections.sort(postingArrayList, new Comparator<ArrayList>() {			//sort using comparator
			public int compare(ArrayList a1, ArrayList a2) {
				return a1.size() - a2.size(); 					
			}
		});
		return postingArrayList;
	}

	public static int findMin(ArrayList<ArrayList> postingArraylist) {				//finding minimum docID for DaatOr
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		int min = -1;
		ArrayList<Integer> currentPosting = new ArrayList<Integer>();
		for (int i = 0; i < postingArraylist.size(); i++) {
			if (postingArraylist.get(i).size() == 0) {
				postingArraylist.remove(i);
			}
		}
		for (int i = 0; i < postingArraylist.size(); i++) {						
			if (postingArraylist.get(i).size() > 0) {
				currentPosting = postingArraylist.get(i);
				postingPointers.add(currentPosting.get(0));
			}
		}
		if (postingPointers.size()>1) {
			min = postingPointers.get(0);
			for (int i = 1; i < postingPointers.size(); i++) {
				counterDAATOr++;											//increment counter of DaatOr as comparison is made
				if ((postingPointers.get(i)) < min) {
					min = postingPointers.get(i);
				}
			}
		}
		else if(postingPointers.size()==1)
		{
			min = postingPointers.get(0);
		}
		return min;
	}

	public static ArrayList<Integer> TAATAnd(String query, HashMap<String, TreeSet<Integer>> Map) {			//function for TAATAnd
		String querySplit[] = query.split(" ");																//split the queries
		ArrayList<ArrayList> postingArraylist = sortPostingList(query, Map);								//sort the query postings by size
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(postingArraylist.get(0));
		ArrayList<Integer> termTAATAnd = new ArrayList<Integer>();
		int countTAnd = 0;
		for (int queryterm = 1; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(postingArraylist.get(queryterm));
			int i = 0, j = 0;
			while (i < temp.size() && j < termpostings.size()) {					//temp contains 1st query's DocIds termpostings contains other query's docIds
				countTAnd++;
				if (temp.get(i).equals(termpostings.get(j))) {
					termTAATAnd.add(temp.get(i));
					i++;
					j++;
				} else if (temp.get(i) < termpostings.get(j)) {
					i++;
				}

				else if (i == (temp.size() - 1) && !temp.get(i).equals(termpostings.get(j))) {

					j++;
				} else if (i < temp.size() && temp.get(i) > termpostings.get(j)) {

					j++;
				}
			}
			temp.removeAll(temp);				//removes elements of temp
			temp.addAll(termTAATAnd);				//adds elements of termpostings to temp
			termTAATAnd.removeAll(termTAATAnd);			//removes elements of intermediate termtaatand
		}
		termTAATAnd.addAll(temp);					//adds values of temp to final termtaatand
		PrintingTaatAND(query, termTAATAnd, termTAATAnd.size(), countTAnd);
		return termTAATAnd;
	}

	public static ArrayList<Integer> TAATOr(String query, HashMap<String, TreeSet<Integer>> Map) {		//function to compute TAATOr
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = sortPostingList(query, Map);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(postingArraylist.get(0));
		ArrayList<Integer> termTAATOr = new ArrayList<Integer>();
		int countTOr = 0;
		for (int queryterm = 1; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termpostings = new ArrayList<Integer>();				
			termpostings.addAll(postingArraylist.get(queryterm));
			int i = 0, j = 0;
			while (i < temp.size() && j < termpostings.size()) {		
			Collections.sort(termTAATOr);
			if (temp.get(i).equals(termpostings.get(j))) {			//if both docIds are equal	
				countTOr++;
				if (!(i == (temp.size() - 1))){					//i is not the last element of temp
				i++;
				j++;
				} 
				else
				{
				j++;
				} 
				}
				else if (temp.get(i) < termpostings.get(j) && !(i == (temp.size() - 1))) {		
					i++;
					countTOr++;
				} else if (temp.get(i) > termpostings.get(j)) {
					termTAATOr.add(termpostings.get(j));
					j++;
					countTOr++;
				} else if (i == (temp.size() - 1) && !temp.get(i).equals(termpostings.get(j))) {
					termTAATOr.add(termpostings.get(j));
					j++;
					
				}
			}
			temp.addAll(termTAATOr);
			Collections.sort(temp);
			termTAATOr.removeAll(termTAATOr);
		}
		Collections.sort(temp);
		termTAATOr = temp;
		PrintingTaatOR(query, termTAATOr, termTAATOr.size(), countTOr);
		return termTAATOr;
	}

	public static ArrayList<Integer> DAATAnd(String query, HashMap<String, TreeSet<Integer>> Map) {		//function to implement DAATAnd
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = new ArrayList<ArrayList>();
		postingArraylist = sortPostingList(query, Map);
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		ArrayList<Integer> DAATAnd = new ArrayList<Integer>();

		for (int queryterm = 0; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termPostings = new ArrayList<Integer>();
			termPostings.addAll(postingArraylist.get(queryterm));
			postingPointers.add(termPostings.get(0));
		}
		int max=0;
		counterDAATAnd = 0;
		while (postingArraylist.size() > 0) {
			max = findMax(postingPointers);						//compute the maximum of Docid for DAATAnd
			outerloop: for (int i = 0; i < postingArraylist.size(); i++) {
				ArrayList<Integer> currentPosting = new ArrayList<Integer>();
				currentPosting = postingArraylist.get(i);
				if (currentPosting.size() > 0 && currentPosting.get(0) == max) {
					if (findEqual(postingPointers) == 1) {				
						counterDAATAnd++;
						DAATAnd.add(postingPointers.get(0));
						for (int p = 0; p < postingArraylist.size(); p++) {
							ArrayList<Integer> samePosting = new ArrayList<Integer>();
							samePosting = postingArraylist.get(p);
						if (samePosting.size() > 1){
							postingPointers.remove(p);
							postingPointers.add(p, samePosting.get(1));
						}
						samePosting.remove(0);
						}
						break outerloop;                  //gets out of the for loop if the pointers are pointing to the same docID
					}
					
				}
				while (currentPosting.size() > 0 && currentPosting.get(0) < max) {
					counterDAATAnd++;
					if (postingPointers.get(i) != 0)
						postingPointers.remove(i);
					if (currentPosting.size() > 1)
						postingPointers.add(i, currentPosting.get(1));
					currentPosting.remove(0);
				}
					if (currentPosting.size() == 0) {
					postingArraylist.remove(i);
					postingArraylist.removeAll(postingArraylist);
				}
			}
		}
		PrintingDaatAnd(query, DAATAnd, DAATAnd.size(), counterDAATAnd);
		return DAATAnd;
	}

	public static int findMax(ArrayList<Integer> postingPointers) {				//function for finding max DocID for DAATAnd
		int max = postingPointers.get(0);
		if (findEqual(postingPointers) == 1){
			return max;
		}
		else {
			
			for (int i = 1; i < postingPointers.size(); i++) {
				counterDAATAnd++;								//counter increments when a comparison is made
				if ((postingPointers.get(i)) > max) {
					max = postingPointers.get(i);
				}
			}
			return max;
		}
	}

	public static int findEqual(ArrayList<Integer> postingPointers) {				// finds equality for pointers

		int equality = 0;
		int temp = postingPointers.get(0);
		for (int i = 1; i < postingPointers.size(); i++) {
			if ((postingPointers.get(i)) == temp)
				equality = equality + 1;
		}
		if (equality == postingPointers.size() - 1) 			//all pointers are pointing to equal DocIds
			return 1;
		else
			return 0;
	}

	public static ArrayList<Integer> DAATOr(String query, HashMap<String, TreeSet<Integer>> Map) {			//function to compute DAATOr
		String querySplit[] = query.split(" ");
		ArrayList<ArrayList> postingArraylist = new ArrayList<ArrayList>();
		 postingArraylist = sortPostingList(query, Map);
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		ArrayList<Integer> DAATOr = new ArrayList<Integer>();
		
		for (int queryterm = 0; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termPostings = new ArrayList<Integer>();
			termPostings.addAll(postingArraylist.get(queryterm));
			postingPointers.add(termPostings.get(0));
		}
		counterDAATOr=0;
		while (postingArraylist.size() > 1) {
			int min = findMin(postingArraylist);
			if (min == -1) {
				break;
			}
			DAATOr.add(min);
			counterDAATOr++;
			for (int i = 0; i < postingArraylist.size(); i++) {
				ArrayList<Integer> currentPosting = new ArrayList<Integer>();
				currentPosting = postingArraylist.get(i);
					if (currentPosting.size() > 0 && currentPosting.get(0) == min) {
					currentPosting.remove(0);
				}
			}
		}
		if (postingArraylist.size() ==1) {
			ArrayList<Integer> currentPosting = new ArrayList<Integer>();
			currentPosting = postingArraylist.get(0);
			if (currentPosting.size() > 0 ) {
				DAATOr.addAll(currentPosting);
			}
		}
		PrintingDaatOr(query, DAATOr, DAATOr.size(), counterDAATOr);
		return DAATOr;
	}
}