package org.dromara.jpom.transport.protocol;

import org.dromara.jpom.transport.netty.enums.MessageCmd;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class TextMessage implements Message {

    private final String id;
    private final MessageCmd cmd;
    private final Map<String, String> header;
    private final String content;

    public TextMessage(Map<String, String> header, String content) {
        this.id = header.getOrDefault("id", UUID.randomUUID().toString());
        this.cmd = MessageCmd.TEXT;
        this.header = header;
        this.content = content;
    }

    @Override
    public String messageId() {
        return id;
    }

    @Override
    public MessageCmd cmd() {
        return cmd;
    }

    @Override
    public Map<String, String> header() {
        return header;
    }

    @Override
    public byte[] content() {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public String text() {
        return content;
    }
}
