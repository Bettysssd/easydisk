package com.easypan.entity.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor


public class PaginationResultVO<T> {
	private Integer totalCount;
	private Integer pageSize;
	private Integer pageNo;
	private Integer pageTotal;
	private List<T> list = new ArrayList<T>();

	public PaginationResultVO(Integer totalCount, Integer pageSize, Integer pageNo, List<T> list) {
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.list = list;
	}

	public PaginationResultVO(Integer totalCount, Integer pageSize, Integer pageNo, Integer pageTotal, List<T> list) {
		if (pageNo == 0) {
			pageNo = 1;
		}
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.pageTotal = pageTotal;
		this.list = list;
	}

}
