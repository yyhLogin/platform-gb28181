package com.yyh.gb28181.entity;

import lombok.Data;

/**
 * @author: yyh
 * @date: 2021-12-09 10:32
 * @description: Catalog
 **/
@Data
public class CatalogResponse {
    /**
     *
     */
    private String deviceId;

    private String CmdType;

    private Long SN;

    private Integer SumNum;

}
