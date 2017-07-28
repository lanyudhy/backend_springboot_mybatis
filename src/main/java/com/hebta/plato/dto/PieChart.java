package com.hebta.plato.dto;

import java.util.List;

public class PieChart {

	private List<String> legend;
	private List<PieChartElement> data;
	public List<String> getLegend() {
		return legend;
	}
	public void setLegend(List<String> legend) {
		this.legend = legend;
	}
	public List<PieChartElement> getData() {
		return data;
	}
	public void setData(List<PieChartElement> data) {
		this.data = data;
	}

}
