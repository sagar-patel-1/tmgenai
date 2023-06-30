package com.genai.tmgenai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.ParagraphSplitter;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.DocumentType.PDF;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.joining;

@Service
public class FileEmbeddingService {

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    public void embedFile(MultipartFile multipartFile,String fileId) throws IOException {

        File file = new File("/Users/amankumar/Downloads"  + fileId + ".pdf");
        multipartFile.transferTo(file);
        DocumentLoader documentLoader = DocumentLoader.from(Paths.get(file.getPath()), PDF);
        Document document = documentLoader.load();


        // Split document into segments (one paragraph per segment)

        DocumentSplitter splitter = new ParagraphSplitter();
//        List<DocumentSegment> documentSegments = splitter.split(document);



        // Embed segments (convert them into semantic vectors)

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

     //   List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();



        // Store embeddings into embedding store for further search / retrieval

        PineconeEmbeddingStore pinecone = PineconeEmbeddingStore.builder()
                .apiKey("1d0899b3-7abf-40be-a267-ac208d572ed3") // https://app.pinecone.io/organizations/xxx/projects/yyy:zzz/keys
                .environment("asia-southeast1-gcp-free")
                .projectName("bca6a53")
                .index("documents") // make sure the dimensions of the Pinecone index match the dimensions of the embedding model (1536 for text-embedding-ada-002)
                .build();
//
       // pinecone.addAll(embeddings, documentSegments);

        String question = "what is the value for policy no?";

        Embedding questionEmbedding = embeddingModel.embed(question).get();



        // Find relevant embeddings in embedding store by semantic similarity

        List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 2);




        // Create a prompt for the model that includes question and relevant embeddings

        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability :\n"
                        + "\n"
                        + "Question:\n"
                        + "{{question}}\n"
                        + "\n"
                        + "Base your answer on the below information from a policy document: \n"
                        + "{{information}}");

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().get().text())
                .collect(joining("\n\n"));

        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);


        // Send prompt to the model

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(GPT_3_5_TURBO)
                .temperature(1.0)
                .logResponses(true)
                .logRequests(true)
                .build();

        AiMessage aiMessage = chatModel.sendUserMessage(prompt).get();


        // See an answer from the model

        String answer = aiMessage.text();
        System.out.println(answer);
    }
}
