package com.beardlesscat.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientConfig {
    private String host ;
    private int port ;
}
