package com.genai.tmgenai.service;

import dev.langchain4j.data.document.*;
import dev.langchain4j.data.document.splitter.CharacterSplitter;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.PineconeEmbeddingStoreImpl;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static dev.langchain4j.data.document.DocumentType.PDF;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;


public class Test {
    public static void main(String[] args) {
        try {
            testDates();
//            testMetadata();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testDates() throws ParseException {
        Date currentDate = LocalDateTime.now().toDate();
        Date resultsExpiryDate = getTodayEndDate("");
        System.out.println(currentDate);
        System.out.println(resultsExpiryDate);
        System.out.println(resultsExpiryDate.compareTo(currentDate) < 0);
        DateFormat df = new SimpleDateFormat("YYYY-MM-DD");
        Date expi=new SimpleDateFormat("dd/MM/yyyy").parse("04/07/2023");
        System.out.println(expi);
        Long days = getDifferenceInDays(currentDate, expi);
        System.out.println(days);
        System.out.println(expi.compareTo(currentDate) < 0);


    }

    public static Long getDifferenceInDays(Date startDate, Date endDate) {
        Long diff = null;
        if (startDate != null && endDate != null) {
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            startCal.setTime(startDate);
            endCal.setTime(endDate);
            diff = (endCal.getTimeInMillis() - startCal.getTimeInMillis())/86400000;
        }
        return diff;
    }


    private static Date getTodayEndDate(String expectedTime) {
        Date today = new Date();

        DateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

        DateTime jodaTime = new DateTime();

        int d = jodaTime.getDayOfMonth();
        String day = "" + (d);
        if (d < 10) {
            day = "0" + (d);
        }

        int m = jodaTime.getMonthOfYear();
        String month = "" + m;
        if (m < 10) {
            month = "0" + m;
        }

        int y = jodaTime.getYear();
        String year = "" + y;

        try {
            if (expectedTime == null) {
                today = formatter.parse(month + "/" + day + "/" + year + " 23:59:59");
            }
            else {
                today = formatter.parse(month + "/" + day + "/" + year + " " + expectedTime);
            }

        }
        catch (Exception e) {

        }

        return today;
    }

    static void testMetadata() {
        File file = new File("/Users/sagarpatel/Downloads/ProblemStatement.pdf");
        DocumentLoader documentLoader = DocumentLoader.from(Paths.get(file.getPath()), PDF);
        Document document = documentLoader.load();

        Metadata metadata = new Metadata();
        metadata.add("fileId","SAGAR_1");
        document = new Document("Hi Sagar This is Text message",metadata);

        // Split document into segments (one paragraph per segment)
        DocumentSplitter splitter = new SentenceSplitter();
//        DocumentSplitter splitter = new CharacterSplitter(500, 10);
        List<DocumentSegment> documentSegments = splitter.split(document);

//        for (DocumentSegment documentSegment : documentSegments) {
//            documentSegment.metadata().add("fileId", "SAGAR_1");
//        }

        List<DocumentSegment> finalSegments = documentSegments.stream().findFirst().stream().toList();

        // Embed segments (convert them into semantic vectors)
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey("test") // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(finalSegments).get();


        // Store embeddings into embedding store for further search / retrieval
//        PineconeEmbeddingStore pinecone = PineconeEmbeddingStore.builder()
//                .apiKey("1d0899b3-7abf-40be-a267-ac208d572ed3") // https://app.pinecone.io/organizations/xxx/projects/yyy:zzz/keys
//                .environment("asia-southeast1-gcp-free")
//                .projectName("bca6a53")
//                .index("documents") // make sure the dimensions of the Pinecone index match the dimensions of the embedding model (1536 for text-embedding-ada-002)
//                .build();
//
        PineconeEmbeddingStoreImpl pincone = new PineconeEmbeddingStoreImpl("test", "asia-southeast1-gcp-free", "bca6a53", "documents", "default");
        pincone.addAll(embeddings, finalSegments);


        List<String> resp = pincone.addAll(embeddings, finalSegments);
        System.out.println(resp);
    }
}
