amalto.namespace("amalto.itemsbrowser");

amalto.itemsbrowser.NavigatorPanel = function(restServiceUrl, id, concept,
		cluster) {
	var NAVIGATOR_NODE_IN_ENTITY_TYPE = 1;
	var NAVIGATOR_NODE_OUT_ENTITY_TYPE = 2;
	var NAVIGATOR_NODE_VALUE_TYPE = 3;
	var NAVIGATOR_NOD_COLOR_SELECTED = "rgb(255, 2, 42)";
	var NAVIGATOR_NOD_COLOR_INBOUND = "rgb(24, 2, 255)";
	var NAVIGATOR_NOD_COLOR_OUTBOUND = "rgb(2, 255, 145)";
	var NAVIGATOR_NOD_COLOR_DATA = "rgb(231, 253, 6)";
	var NAVIGATOR_NODE_IMAGE_INBOUND = "secure/img/navigator_relation_in.png";
	var NAVIGATOR_NODE_IMAGE_OUTBOUND = "secure/img/navigator_relation_out.png";
	var NAVIGATOR_NODE_IMAGE_DATA = "secure/img/navigator_data.png";

	var selectNode;
	var image_width = 30;
	var image_height = 30;
	var text_dx = -15;
	var text_dy = 15;
	var width = 800;
	var height = 800;
	var xOffsetIn;
	var yOffsetIn;
	var xOffsetOut;
	var yOffsetOut;
	var pageSize = 5;
	var filterValue = '';

	var zoom = d3.behavior.zoom().scaleExtent([ -15, 100 ]).on("zoom", zoomed);
	var svg = d3.select("#navigator").append("svg").attr("width", width).attr(
			"height", height).append("g").call(zoom);
	var rect = svg.append("rect").attr("width", width).attr("height", height)
			.style("fill", "none").style("pointer-events", "all");
	container = svg.append("g");

	var color = d3.scale.category10();
	var dataset = [ {
		label : "out",
		value : 25
	}, {
		label : "set",
		value : 25
	}, {
		label : "in",
		value : 25
	}, {
		label : "info",
		value : 25
	} ];
	var pie = d3.layout.pie().value(function(d) {
		return d.value;
	});
	pie.startAngle(0.8);
	pie.endAngle(7.1);
	var piedata = pie(dataset);

	var outerRadius = 65;
	var innerRadius = 35;

	var arc = d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius);

	var typeCluster = d3.layout.cluster().size([ 200, 150 ]);

	var diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.y, d.x ];
	});

	var nodeList = [];
	var linkList = [];
	var container;
	var links;
	var nodes;
	var link;
	var node;
	var link_text;
	var node_text;
	var typeLinks;
	var typeNodes;
	var typeLink;
	var typeNode;
	var drag;
	var force;
	init(id, concept, cluster);

	function paint() {
		link = link.data(links);
		node = node.data(nodes);
		link.enter().append("line").style("stroke-width", 1).style("stroke",
				"#ccc");
		node.enter().append("image").attr("width", image_width).attr("height",
				image_height).attr("x", "8px").attr("y", "8px").attr(
				"identifier", function(d) {
					return getIdentifier(d)
				}).attr("xlink:href", function(d) {
			return getImage(d)
		}).each(function(d, i) {
			d3.select(this).call(drag);
			d3.select(this).on("click", showMenu);
			d3.select(this).on("dblclick", dblclick);
			d3.select(this).on("mouseover", mouseover);
			d3.select(this).on("mouseout", mouseout);
		});

		link_text = link_text.data(links);
		link_text.enter().append("text").style("font-weight", "bold").style(
				"fill-opacity", "0.0").text(function(d) {
			return d.navigator_line_label;
		});
		node_text = node_text.data(nodes);
		node_text.enter().append("text").style("fill", "black").attr("dx",
				text_dx).attr("dy", text_dy).text(function(d) {
			return d.navigator_node_label;
		});
		force.start();
	}

	function paintTypeCluster(root) {
		hiddenTypeCluster();
		var elementId;
		var text_x;
		var text_anchor;
		if (root.navigator_node_type === NAVIGATOR_NODE_IN_ENTITY_TYPE) {
			text_dx = -48;
			text_anchor = "start";
			elementId = "cluster_type_in_group_" + getIdentifier(selectNode);
		} else if (root.navigator_node_type === NAVIGATOR_NODE_OUT_ENTITY_TYPE) {
			text_dx = 48;
			text_anchor = "end";
			elementId = "cluster_type_out_group_" + getIdentifier(selectNode);
		}
		if ((svg.select("#" + elementId)[0])[0] === null) {
			var typeGroup = container.append("g").attr("id", elementId).style(
					"display", "inline");
			var typeNodes = typeCluster.nodes(root);
			var typeLinks = typeCluster.links(typeNodes);
			var xOffset = selectNode.x - root.y;
			var yOffset = selectNode.y - root.x;
			var typeNode = typeGroup
					.selectAll(".node")
					.data(typeNodes)
					.enter()
					.append('g')
					.style('font', '12px sans-serif')
					.attr(
							'transform',
							function(node) {
								node.x = node.x + yOffset;
								node.y = node.y + xOffset;
								if (root.navigator_node_type === NAVIGATOR_NODE_IN_ENTITY_TYPE) {
									if (node.name !== 'root') {
										node.y = selectNode.x
												+ (selectNode.x - node.y);
									}
								}
								return 'translate(' + node.y + ',' + node.x
										+ ')';
							});

			var typeLink = typeGroup.selectAll(".link").data(typeLinks).enter()
					.append("path").style("fill", "none").style("stroke",
							"#ccc").style("stroke-width", "1.5px").attr("d",
							diagonal);

			typeNode.each(function(d, i) {
				if (d.navigator_node_concept !== 'root') {
					d3.select(this).append("image").attr("width", image_width)
							.attr("height", image_height).attr("x", "-15px")
							.attr("y", "-15px").attr("identifier", function(d) {
								return getIdentifier(d);
							}).attr("xlink:href", function(d) {
								return getImage(d);
							}).on("click", click);
				}
			});

			typeNode.append("text").attr("dx", function(node) {
				return text_dx;
			}).attr("dy", 3).style("text-anchor", function(d) {
				return text_anchor;
			}).text(function(d) {
				if (d.navigator_node_concept !== 'root') {
					return d.navigator_node_concept;
				}
			});
		} else {
			svg.select("#" + elementId).style("display", "inline");
		}
	}

	function menuClick(arc) {
		if ('info' === arc.data.label) {
			hiddenTypeCluster();
			if (NAVIGATOR_NODE_VALUE_TYPE == selectNode.navigator_node_type) {
				amalto.navigator.Navigator.openRecord(
						selectNode.navigator_node_ids,
						selectNode.navigator_node_concept);
			}
		}
		if ('in' === arc.data.label) {
			if (NAVIGATOR_NODE_VALUE_TYPE == selectNode.navigator_node_type) {
				Ext.Ajax
						.request({
							url : restServiceUrl + '/data/' + cluster
									+ '/inBoundTypes/'
									+ selectNode.navigator_node_concept + '/'
									+ selectNode.navigator_node_ids,
							method : 'GET',
							success : function(response, options) {
								var newNodes = eval('(' + response.responseText
										+ ')');
								var newNodes = eval('(' + response.responseText
										+ ')');
								var root = {
									"navigator_node_concept" : "root",
									"navigator_node_type" : NAVIGATOR_NODE_IN_ENTITY_TYPE,
									"children" : newNodes
								};
								paintTypeCluster(root);
							},
							failure : function() {
							}
						});
			}
		}
		if ('out' === arc.data.label) {
			if (NAVIGATOR_NODE_VALUE_TYPE == selectNode.navigator_node_type) {
				Ext.Ajax
						.request({
							url : restServiceUrl + '/data/' + cluster
									+ '/outBoundTypes/'
									+ selectNode.navigator_node_concept + '/'
									+ selectNode.navigator_node_ids,
							method : 'GET',
							success : function(response, options) {
								var newNodes = eval('(' + response.responseText
										+ ')');
								var root = {
									"navigator_node_concept" : "root",
									"navigator_node_type" : NAVIGATOR_NODE_OUT_ENTITY_TYPE,
									"children" : newNodes
								};
								paintTypeCluster(root);
							},
							failure : function() {
							}
						});
			}
		}
		if ('set' === arc.data.label) {
			hiddenTypeCluster();
			if (NAVIGATOR_NODE_VALUE_TYPE == selectNode.navigator_node_type) {
				var settingWindow = new Ext.Window({
					id : 'settingWindow',
					title : 'Inbound setting window',
					width : 200,
					height : 100,
					modal : true,
					layout : 'form',
					bodyStyle : "padding:10px",
					frame : true,
					items : [ {
						id : 'settingForm',
						xtype : 'form',
						labelWidth : 60,
						labelAlign : 'right',
						buttonAlign : 'center',
						items : [  {
							xtype : 'textfield',
							id : 'filterValue',
							name : 'filterValue',
							fieldLabel : 'Search',
							width : 'auto',
							value : filterValue
						},{
							xtype : 'numberfield',
							id : 'pageSize',
							name : 'pageSize',
							fieldLabel : 'Page Size',
							width : 'auto',
							value : pageSize
						} ],
						buttons : [ {
							xtype : 'button',
							text : "ok",
							handler : function() {
								pageSize = Ext.getCmp('pageSize').value;
								filterValue = Ext.getCmp('filterValue').getRawValue();
								Ext.getCmp('settingWindow').close();
							}
						}, {
							xtype : 'button',
							text : 'cancel',
							handler : function() {
								Ext.getCmp('settingWindow').close();
							}
						} ]
					} ]
				});
				Ext.getCmp('settingWindow').show();
			}
		}
	}

	function click(d, i) {
		hiddenTypeCluster();
		if (NAVIGATOR_NODE_IN_ENTITY_TYPE == d.navigator_node_type) {
			if (d.pageNumber === undefined) {
				d.pageNumber = 1;
			}
			if ((d.pageNumber == 1 || d.pageNumber <= d.pageCount)) {
				Ext.Ajax
						.request({
							url : restServiceUrl + '/data/' + cluster
									+ '/inBoundRecords/'
									+ d.navigator_node_concept,
							method : 'GET',
							params : {
								foreignKeyPath : d.navigator_node_foreignkey_path,
								foreignKeyValue : d.navigator_node_foreignkey_value,
								filterValue : filterValue,
								start : (d.pageNumber - 1) * pageSize,
								limit : pageSize
							},
							success : function(response, options) {
								var resultObject = eval('('
										+ response.responseText + ')');
								if (d.pageNumber == 1) {
									if ((resultObject.totalCount % pageSize) == 0) {
										d.pageCount = Math
												.floor(resultObject.totalCount
														/ pageSize);
									} else {
										d.pageCount = Math
												.floor(resultObject.totalCount
														/ pageSize) + 1;
									}
								}
								var newNodes = resultObject.result;
								for ( var i = 0; i < newNodes.length; i++) {
									var node = newNodes[i];
									var newNode = {
										x : selectNode.x
												+ getRandomInt(-15, 15),
										y : selectNode.y
												+ getRandomInt(-15, 15),
										navigator_node_ids : node.navigator_node_ids,
										navigator_node_concept : node.navigator_node_concept,
										navigator_node_type : node.navigator_node_type,
										navigator_node_label : node.navigator_node_label,
										navigator_node_expand : false
									};
									nodes.push(newNode);
									var newLink = {
										source : selectNode,
										target : newNode,
										navigator_node_type : node.navigator_node_type,
										navigator_line_label : node.navigator_line_label,
										navigator_node_concept : node.navigator_node_concept
									};
									links.push(newLink);
								}
								paint();
								d.pageNumber = d.pageNumber + 1;
							},
							failure : function() {

							}
						});
			}
		} else if (NAVIGATOR_NODE_OUT_ENTITY_TYPE == d.navigator_node_type) {

			var idArray = [];
			for (i = 0; (i < pageSize && i < d.navigator_node_ids.length); i++) {
				idArray[i] = d.navigator_node_ids[i];
			}
			if (idArray.length > 0) {
				Ext.Ajax
						.request({
							url : restServiceUrl + '/data/' + cluster
									+ '/records/' + d.navigator_node_concept,
							method : 'GET',
							params : {
								ids : idArray
							},
							success : function(response, options) {
								var newNodes = eval('(' + response.responseText
										+ ')');
								for ( var i = 0; i < newNodes.length; i++) {
									var node = newNodes[i];
									var newNode = {
										x : selectNode.x
												+ getRandomInt(-15, 15),
										y : selectNode.y
												+ getRandomInt(-15, 15),
										navigator_node_ids : node.navigator_node_ids,
										navigator_node_concept : d.navigator_node_concept,
										navigator_node_type : node.navigator_node_type,
										navigator_node_label : node.navigator_node_label,
										navigator_node_expand : false
									};
									nodes.push(newNode);
									var newLink = {
										source : selectNode,
										target : newNode,
										navigator_node_type : node.navigator_node_type,
										navigator_line_label : node.navigator_line_label,
										navigator_node_concept : node.navigator_node_concept
									};
									links.push(newLink);
									d.navigator_node_ids.shift();
								}
								paint();
							},
							failure : function() {
							}
						});
			}
		}
	}

	var dblclick = function(d, i) {
		d.fixed = false;
	}

	var getRandomInt = function(min, max) {
		return Math.floor(Math.random() * (max - min + 1) + min);
	}

	function showMenu(d, i) {
		if (selectNode !== undefined) {
			hiddenTypeCluster();
		}

		var elementId = "menu_" + getIdentifier(d);
		d3.select(this).transition().duration(750).attr("width", 35).attr(
				"height", 35);
		var arcs = container.append("g").attr("id", elementId).selectAll("g")
				.data(piedata).enter().append("g").attr("transform",
						"translate(" + (d.x) + "," + (d.y) + ")");

		arcs.append("path").attr("fill", function(d, i) {
			return color(i);
		}).attr("d", function(d) {
			return arc(d);
		});

		arcs.append("text").attr("transform", function(d) {
			return "translate(" + arc.centroid(d) + ")";
		}).attr("text-anchor", "middle").text(function(d) {
			return d.data.label;
		});

		arcs.on("click", menuClick);
		// arcs.on("mouseout", menuMouseout);
		selectNode = d;
	}

	function mouseover(d, i) {
		link_text.style("fill-opacity", function(link) {
			if (link.source === d || link.target === d) {
				return 1.0;
			} else {
				return 0.0;
			}
		});
	}

	function mouseout(d, i) {
		link_text.style("fill-opacity", "0.0");
		d3.select(this).transition().duration(750).attr("width", image_width)
				.attr("height", image_height);
	}

	// function menuMouseout(arc) {
	// var elementId = "#menu_" + getIdentifier(selectNode);
	// svg.select(elementId).remove();
	// }

	function tick() {
		node
				.forEach(function(d, i) {
					d.x = d.x - image_width / 2 < 0 ? image_width / 2 : d.x;
					d.x = d.x + image_width / 2 > width ? width - image_width
							/ 2 : d.x;
					d.y = d.y - image_height / 2 < 0 ? image_height / 2 : d.y;
					d.y = d.y + image_height / 2 + text_dy > height ? height
							- image_height / 2 - text_dy : d.y;
				});
		link.attr("x1", function(d) {
			return d.source.x;
		}).attr("y1", function(d) {
			return d.source.y;
		}).attr("x2", function(d) {
			return d.target.x;
		}).attr("y2", function(d) {
			return d.target.y;
		})
		node.attr("x", function(d) {
			return d.x - image_width / 2;
		}).attr("y", function(d) {
			return d.y - image_height / 2;
		});
		link_text.attr("x", function(d) {
			return (d.source.x + d.target.x) / 2;
		}).attr("y", function(d) {
			return (d.source.y + d.target.y) / 2;
		});
		node_text.attr("x", function(d) {
			return d.x;
		}).attr("y", function(d) {
			return d.y + image_width / 2;
		});
	}

	function zoomed() {
		svg.attr("transform", "translate(" + d3.event.translate + ")scale("
				+ d3.event.scale + ")");
	}

	function init(id, concept, cluster) {
		// svg = d3.select("#navigator").append("svg").attr("width",
		// width).attr(
		// "height", height).append("g").call(zoom);
		// rect = svg.append("rect").attr("width", width).attr("height", height)
		// .style("fill", "none").style("pointer-events", "all");
		// container = svg.append("g");
		var ids = new Array(id);
		Ext.Ajax.request({
			url : restServiceUrl + '/data/' + cluster + '/records/' + concept,
			method : 'GET',
			params : {
				ids : ids
			},
			success : function(response, options) {
				nodes = eval('(' + response.responseText + ')');
				links = [];
				force = d3.layout.force().nodes(nodes).links(links).size(
						[ width, height ]).linkDistance(150).charge([ -400 ]);
				drag = force.drag().on("dragstart", function(d, i) {
					d.fixed = true;
				});

				links = force.links();
				nodes = force.nodes();
				link = container.append("g").attr("id",
						"navigator_data_link_group").selectAll(".link");
				node = container.append("g").attr("id",
						"navigator_data_node_group").selectAll("image");
				link_text = container.append("g").attr("id",
						"navigator_text_link_group").selectAll(".linetext");
				node_text = container.append("g").attr("id",
						"navigator_text_node_group").selectAll(".nodetext");
				force.on("tick", tick);
				paint();
			},
			failure : function() {
			}
		});
	}

	function getColor(o) {
		if (NAVIGATOR_NODE_IN_ENTITY_TYPE == o.navigator_node_type) {
			return NAVIGATOR_NOD_COLOR_INBOUND;
		} else if (NAVIGATOR_NODE_OUT_ENTITY_TYPE == o.navigator_node_type) {
			return NAVIGATOR_NOD_COLOR_OUTBOUND;
		} else {
			return NAVIGATOR_NOD_COLOR_DATA;
		}
	}

	function getImage(o) {
		if (NAVIGATOR_NODE_IN_ENTITY_TYPE == o.navigator_node_type) {
			return NAVIGATOR_NODE_IMAGE_INBOUND;
		} else if (NAVIGATOR_NODE_OUT_ENTITY_TYPE == o.navigator_node_type) {
			return NAVIGATOR_NODE_IMAGE_OUTBOUND;
		} else {
			return NAVIGATOR_NODE_IMAGE_DATA;
		}
	}

	function getIdentifier(o) {
		var identifier;
		if (o.navigator_node_concept !== undefined) {
			identifier = 'navigator_' + o.navigator_node_concept;
		}
		if (o.navigator_node_ids !== undefined) {
			identifier = identifier + '_' + o.navigator_node_ids;
		}
		return identifier;
	}

	function hiddenTypeCluster() {
		svg.select("#cluster_type_in_group_" + getIdentifier(selectNode))
				.style("display", "none");
		svg.select("#cluster_type_out_group_" + getIdentifier(selectNode))
				.style("display", "none");
		var elementId = "#menu_" + getIdentifier(selectNode);
		svg.select(elementId).remove();
	}
}