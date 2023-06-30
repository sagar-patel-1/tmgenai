package com.genai.tmgenai;

import com.google.protobuf.Value;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.PineconeEmbeddingStoreImpl;
import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import io.pinecone.proto.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PineConeEmbeddingstoreCustomImpl extends PineconeEmbeddingStoreImpl {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String METADATA_DOCUMENT_SEGMENT_TEXT = "document_segment_text";
    private final PineconeConnection connection;
    private final String nameSpace;

    public PineConeEmbeddingstoreCustomImpl(String apiKey, String environment, String projectName, String index, String nameSpace) {
        super(apiKey, environment, projectName, index, nameSpace);
        PineconeClientConfig configuration = (new PineconeClientConfig()).withApiKey(apiKey).withEnvironment(environment).withProjectName(projectName);
        PineconeClient pineconeClient = new PineconeClient(configuration);
        PineconeConnectionConfig connectionConfig = (new PineconeConnectionConfig()).withIndexName(index);
        this.connection = pineconeClient.connect(connectionConfig);
        this.nameSpace = nameSpace == null ? "default" : nameSpace;
    }

    @Override
    public List<EmbeddingMatch<DocumentSegment>> findRelevant(Embedding referenceEmbedding, int maxResults) {
        QueryVector queryVector = QueryVector.newBuilder().addAllValues(referenceEmbedding.vectorAsList()).setTopK(maxResults).setNamespace(this.nameSpace).build();
        QueryRequest queryRequest = QueryRequest.newBuilder().addQueries(queryVector).setTopK(maxResults).build();
        List<String> matchedVectorIds = (List)((SingleQueryResults)this.connection.getBlockingStub().query(queryRequest).getResultsList().get(0)).getMatchesList().stream().map(ScoredVector::getId).collect(Collectors.toList());
        if (matchedVectorIds.isEmpty()) {
            return Collections.emptyList();
        } else {
            Collection<Vector> matchedVectors = this.connection.getBlockingStub().fetch(FetchRequest.newBuilder().addAllIds(matchedVectorIds).setNamespace(this.nameSpace).build()).getVectorsMap().values();
            return (List)matchedVectors.stream().map(PineConeEmbeddingstoreCustomImpl::toEmbeddingMatch).collect(Collectors.toList());
        }
    }

    private static EmbeddingMatch<DocumentSegment> toEmbeddingMatch(Vector vector) {
        Value documentSegmentTextValue = (Value)vector.getMetadata().getFieldsMap().get("document_segment_text");
        return new EmbeddingMatch(vector.getId(), Embedding.from(vector.getValuesList()), createDocumentSegmentIfExists(documentSegmentTextValue));
    }

    private static DocumentSegment createDocumentSegmentIfExists(Value documentSegmentTextValue) {
        return documentSegmentTextValue == null ? null : DocumentSegment.from(documentSegmentTextValue.getStringValue());
    }
}
