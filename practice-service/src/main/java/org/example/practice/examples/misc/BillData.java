package org.example.practice.examples.misc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillData {

    private List<String> orgIds;

    public String orgId;
}
