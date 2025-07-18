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
    @Value("${vector.store.count}")
    private int vectorStoreCount;

    public PdfReferenceDocsLoader(EmbeddingModel embeddingModel, JdbcClient jdbcClient, VectorStore vectorStore){
        this.embeddingModel = embeddingModel;// Note: local llm, tinyllama:latest, is used here since the free version of OpenAI LLM complains about the quota & billing issues. It looks like the embedding process is done by the LLM here as well.
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init(){

        log.info("the pdf doc under resources/docs/ folder is \"{}\"", pdfDoc.getFilename());

        int count = jdbcClient.sql("select count(*) from vector_store")
                .query(Integer.class)
                .single();

        log.info("\n\n\nif wanna insert the pdf file, {}, to vector_store,\nset vector.store.count to {}.\nCurrently, in application.properties\n,   vector.store.count is {}.\n\n", pdfDoc.getFilename(), count, vectorStoreCount);

        // TODO: Note: If you are going to push more PDF files to the vector store,
        //             manually update both
        //               1. vector.store.count
        //               2. pdf.file.path
        //             in application.properties file.
        //             The same PDF file can be inserted repeatedly if you only update vector.store.count !!!
        if(count == vectorStoreCount){//1512){//1508){//1272){//1135){//0) {// Note: when starting up the server, it can only process one PDF file here. One needs to specify the pdf file in application.properties file.
            log.info(" *** inserting {} to vector_store *** ", pdfDoc.getFilename());

            var readerConfig = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(
                            new ExtractedTextFormatter.Builder()
                                    .withNumberOfBottomTextLinesToDelete(0)
                                    .withNumberOfTopPagesToSkipBeforeDelete(0)
                                    .build())
                    .withPagesPerDocument(1)
                    .build();

            var pdfReader = new PagePdfDocumentReader(pdfDoc, readerConfig);
            var tokenizer = new TokenTextSplitter();
            vectorStore.accept(tokenizer.apply(pdfReader.get()));

            log.info("done populating the vector_store table :)");
        }
    }
}
