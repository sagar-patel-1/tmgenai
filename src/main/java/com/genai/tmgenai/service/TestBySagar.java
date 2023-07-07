package com.genai.tmgenai.service;

import com.genai.tmgenai.dto.AutoCompleteDetails;

import java.util.*;


public class TestBySagar {
    public static void main(String[] args) {

        AutocompleteStore autocompleteStore = new AutocompleteStore();
        List<AutoCompleteDetails> questions  = autocompleteStore.getAutocomplete().giveSuggestions("what is", AutoCompleteDetails.VERTICAL.LIFE);

        for(AutoCompleteDetails question: questions)
        {
            System.out.println(question);
        }

//        Autocomplete autocomplete = new Autocomplete();

        // Insert sample questions
//        autocomplete.insert("what are add ons?", "test");
//        autocomplete.insert("what is expiry date?","sagar");
//        autocomplete.insert("what are the terms & condition?");
//        autocomplete.insert("how to claim?");
//        autocomplete.insert("what is cover amount?");
//        autocomplete.insert("what is policy number?");
//        autocomplete.insert("what is registration number?");



        // Get autocomplete suggestions
//        String prefix = "what";
//        List<String> suggestions = autocomplete.autocomplete(prefix);
//
//        // Display suggestions
//        System.out.println("Autocomplete suggestions for prefix \"" + prefix + "\":");
//        for (String suggestion : suggestions) {
//            System.out.println(suggestion);
//        }
    }
}
