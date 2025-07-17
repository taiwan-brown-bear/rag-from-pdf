package com.taiwan_brown_bear.rag_from_pdf.docs;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PdfReferenceDocsLoader {

    private final JdbcClient jdbcClient;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    //@Value("classpath:/docs/spring-boot-reference.pdf")
    @Value("${pdf.file.path}")// read from application.properties instead ...
    private Resource pdfDoc;

    public PdfReferenceDocsLoader(EmbeddingModel embeddingModel, JdbcClient jdbcClient, VectorStore vectorStore){
        this.embeddingModel = embeddingModel;// Note: local llm, tinyllama:latest, is used here since the free version of OpenAI LLM complains about the quota & billing issues. It looks like the word2vec process is done by the LLM here.
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init(){

        log.info("the pdf doc under resources/docs/ folder is \"{}\"", pdfDoc.getFilename());

        Integer count = jdbcClient.sql("select count(*) from vector_store")
                .query(Integer.class)
                .single();

        log.info("# of rows in vector_store table: {}", count);

        if(count == 1512){//1508){//1272){//1135){//0) {
            log.info("since no rows in the vector store table, going to populate the table " +
                    "with the pdf doc under resources/docs/ folder");

            var readerConfig = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(
                            new ExtractedTextFormatter.Builder()
                                    .withNumberOfBottomTextLinesToDelete(0)
                                    .withNumberOfTopPagesToSkipBeforeDelete(0)
                                    .build())
                    .withPagesPerDocument(1)// TODO: check the count later when changing this number ...
                    .build();

            var pdfReader = new PagePdfDocumentReader(pdfDoc, readerConfig);
            var tokenizer = new TokenTextSplitter();
            vectorStore.accept(tokenizer.apply(pdfReader.get()));

            log.info("done populating the vector_store table :)");
        }
    }
}
