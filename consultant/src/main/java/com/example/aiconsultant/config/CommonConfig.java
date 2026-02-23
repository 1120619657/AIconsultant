package com.example.aiconsultant.config;

import com.example.aiconsultant.aiservice.ConsultantService;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class CommonConfig {
    @Autowired
    private OpenAiStreamingChatModel model;
    @Autowired
    private ChatMemoryStore redisChatMemoryStore;
    @Autowired
    private McpToolProvider mcpToolProvider;
    @Autowired
    private McpToolProvider amapToolProvider;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;
    @Bean
    public ConsultantService consultantService(){
        ConsultantService consultantService = AiServices.builder(ConsultantService.class)
                .streamingChatModel(model)
                .chatMemoryProvider(chatMemoryProvider())
                .contentRetriever(contentRetriever()) // RAG 检索增强生成
                .toolProvider(mcpToolProvider)
                .toolProvider(amapToolProvider)// MCP 工具调用
                .build();
        return consultantService;
    }
    //构建会话记忆对象
    @Bean
    public ChatMemory chatMemory(){
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
        return memory;
    }

    //构建ChatMemoryProvider对象
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        ChatMemoryProvider chatMemoryProvider = new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
        return chatMemoryProvider;
    }
    //构建向量数据库操作对象
    //@Bean
    public EmbeddingStore store(){//embeddingStore的对象, 这个对象的名字不能重复,所以这里使用store
        //1.加载文档进内存
        //List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content",new ApachePdfBoxDocumentParser());
        //List<Document> documents = FileSystemDocumentLoader.loadDocuments("C:\\Users\\Administrator\\ideaProjects\\consultant\\src\\main\\resources\\content");
        //2.构建向量数据库操作对象  操作的是内存版本的向量数据库
        //InMemoryEmbeddingStore store = new InMemoryEmbeddingStore();

        //构建文档分割器对象
        DocumentSplitter ds = DocumentSplitters.recursive(500,100);
        //3.构建一个EmbeddingStoreIngestor对象,完成文本数据切割,向量化, 存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                //.embeddingStore(store)
                .embeddingStore(redisEmbeddingStore)
                .documentSplitter(ds)
                .embeddingModel(embeddingModel)
                .build();
        ingestor.ingest(documents);
        return redisEmbeddingStore;
    }


    //构建向量数据库检索对象
    @Bean
    public ContentRetriever contentRetriever(){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .minScore(0.5)
                .maxResults(3)
                .embeddingModel(embeddingModel)
                .build();
    }

}
