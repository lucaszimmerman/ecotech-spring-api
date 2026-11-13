package com.ecotech.api.repository.projections;

import java.util.UUID;

public interface PostCount {
    UUID getPostId();
    long getTotal();
}
