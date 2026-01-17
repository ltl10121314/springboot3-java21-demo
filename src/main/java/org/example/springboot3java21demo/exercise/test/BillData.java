package org.example.springboot3java21demo.exercise.test;

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
