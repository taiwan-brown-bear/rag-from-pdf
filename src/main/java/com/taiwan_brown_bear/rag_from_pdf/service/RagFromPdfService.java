package com.taiwan_brown_bear.rag_from_pdf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RagFromPdfService {

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    @Value("${llm.prompt.template}")
    private Resource template;

    public RagFromPdfService(ChatModel chatModel, VectorStore vectorStore){
        this.chatClient = ChatClient.create(chatModel);
        this.vectorStore = vectorStore;
    }

    //@Command(command = "q")
    public String askQuestion(@DefaultValue(value = "Can you summarize what you know the best ?") String question){

        log.info("template is \"{}\"", template);
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> promptTemplateParameters = new HashMap<>();

        ///////////////////////////////////////////////////////////////
        //
        // Reminder: In the template, we have
        //
        // QUESTION:
        // {input}
        //
        // DOCUMENTS:
        // {documents}
        //
        log.info("{input} is \"{}\"", question);
        promptTemplateParameters.put("input"    , question);
        String documents = String.join("\n", knn(question, 1));
        log.info("{documents} is \"{}\"", documents);
        promptTemplateParameters.put("documents", documents);

        //     ChatResponse  chatResponse  = chatClient.prompt(prompt).call().chatResponse();
        return chatClient
                .prompt(promptTemplate.create(promptTemplateParameters))
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        /*
        return String.join(",",
                "Goldman Sachs Investment Strategy Outlook",
                "Spring Boot Overall Reference Guide",
                "Vanguard US Sector ETF Detailed Look");
         */
    }

    private List<String> knn(String questionMessage, int k){
        List<Document> similarSearchResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(questionMessage)
                        .topK(k)
                        .build());
        return similarSearchResults.stream().map(document -> document.getFormattedContent()).toList();
    }

}
