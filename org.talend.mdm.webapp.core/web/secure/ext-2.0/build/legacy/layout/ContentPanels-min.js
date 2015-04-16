/*
 * Ext JS Library 2.0.2
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ContentPanel=function(B,A,C){if(B.autoCreate){A=B;B=Ext.id()}this.el=Ext.get(B);if(!this.el&&A&&A.autoCreate){if(typeof A.autoCreate=="object"){if(!A.autoCreate.id){A.autoCreate.id=A.id||B}this.el=Ext.DomHelper.append(document.body,A.autoCreate,true)}else{this.el=Ext.DomHelper.append(document.body,{tag:"div",cls:"x-layout-inactive-content",id:A.id||B},true)}}this.closable=false;this.loaded=false;this.active=false;if(typeof A=="string"){this.title=A}else{Ext.apply(this,A)}if(this.resizeEl){this.resizeEl=Ext.get(this.resizeEl,true)}else{this.resizeEl=this.el}this.addEvents({"activate":true,"deactivate":true,"resize":true});if(this.autoScroll){this.resizeEl.setStyle("overflow","auto")}C=C||this.content;if(C){this.setContent(C)}if(A&&A.url){this.setUrl(this.url,this.params,this.loadOnce)}Ext.ContentPanel.superclass.constructor.call(this)};Ext.extend(Ext.ContentPanel,Ext.util.Observable,{tabTip:"",setRegion:function(A){this.region=A;if(A){this.el.replaceClass("x-layout-inactive-content","x-layout-active-content")}else{this.el.replaceClass("x-layout-active-content","x-layout-inactive-content")}},getToolbar:function(){return this.toolbar},setActiveState:function(A){this.active=A;if(!A){this.fireEvent("deactivate",this)}else{this.fireEvent("activate",this)}},setContent:function(B,A){this.el.update(B,A)},ignoreResize:function(A,B){if(this.lastSize&&this.lastSize.width==A&&this.lastSize.height==B){return true}else{this.lastSize={width:A,height:B};return false}},getUpdater:function(){return this.el.getUpdater()},load:function(){this.el.load.apply(this.el,arguments);return this},setUrl:function(A,C,B){if(this.refreshDelegate){this.removeListener("activate",this.refreshDelegate)}this.refreshDelegate=this._handleRefresh.createDelegate(this,[A,C,B]);this.on("activate",this.refreshDelegate);return this.el.getUpdater()},_handleRefresh:function(A,D,C){if(!C||!this.loaded){var B=this.el.getUpdater();B.update(A,D,this._setLoaded.createDelegate(this))}},_setLoaded:function(){this.loaded=true},getId:function(){return this.el.id},getEl:function(){return this.el},adjustForComponents:function(B,A){if(this.resizeEl!=this.el){B-=this.el.getFrameWidth("lr");A-=this.el.getFrameWidth("tb")}if(this.toolbar){var C=this.toolbar.getEl();A-=C.getHeight();C.setWidth(B)}if(this.adjustments){B+=this.adjustments[0];A+=this.adjustments[1]}return{"width":B,"height":A}},setSize:function(C,A){if(this.fitToFrame&&!this.ignoreResize(C,A)){if(this.fitContainer&&this.resizeEl!=this.el){this.el.setSize(C,A)}var B=this.adjustForComponents(C,A);this.resizeEl.setSize(this.autoWidth?"auto":B.width,this.autoHeight?"auto":B.height);this.fireEvent("resize",this,B.width,B.height)}},getTitle:function(){return this.title},setTitle:function(A){this.title=A;if(this.region){this.region.updatePanelTitle(this,A)}},isClosable:function(){return this.closable},beforeSlide:function(){this.el.clip();this.resizeEl.clip()},afterSlide:function(){this.el.unclip();this.resizeEl.unclip()},refresh:function(){if(this.refreshDelegate){this.loaded=false;this.refreshDelegate()}},destroy:function(){this.el.removeAllListeners();var A=document.createElement("span");A.appendChild(this.el.dom);A.innerHTML="";this.el.remove();this.el=null}});Ext.ContentPanel.prototype.getUpdateManager=Ext.ContentPanel.prototype.getUpdater;Ext.GridPanel=function(B,A){this.wrapper=Ext.DomHelper.append(document.body,{tag:"div",cls:"x-layout-grid-wrapper x-layout-inactive-content"},true);this.wrapper.dom.appendChild(B.getGridEl().dom);Ext.GridPanel.superclass.constructor.call(this,this.wrapper,A);if(this.toolbar){this.toolbar.el.insertBefore(this.wrapper.dom.firstChild)}B.monitorWindowResize=false;B.autoHeight=false;B.autoWidth=false;this.grid=B;this.grid.getGridEl().replaceClass("x-layout-inactive-content","x-layout-component-panel")};Ext.extend(Ext.GridPanel,Ext.ContentPanel,{getId:function(){return this.grid.id},getGrid:function(){return this.grid},setSize:function(D,A){if(!this.ignoreResize(D,A)){var C=this.grid;var B=this.adjustForComponents(D,A);C.getGridEl().setSize(B.width,B.height);C.autoSize()}},beforeSlide:function(){this.grid.getView().scroller.clip()},afterSlide:function(){this.grid.getView().scroller.unclip()},destroy:function(){this.grid.destroy();delete this.grid;Ext.GridPanel.superclass.destroy.call(this)}});Ext.NestedLayoutPanel=function(B,A){Ext.NestedLayoutPanel.superclass.constructor.call(this,B.getEl(),A);B.monitorWindowResize=false;this.layout=B;this.layout.getEl().addClass("x-layout-nested-layout")};Ext.extend(Ext.NestedLayoutPanel,Ext.ContentPanel,{setSize:function(D,A){if(!this.ignoreResize(D,A)){var B=this.adjustForComponents(D,A);var C=this.layout.getEl();C.setSize(B.width,B.height);var E=C.dom.offsetWidth;this.layout.layout();if(Ext.isIE&&!this.initialized){this.initialized=true;this.layout.layout()}}},getLayout:function(){return this.layout}});Ext.ScrollPanel=function(D,B,E){B=B||{};B.fitToFrame=true;Ext.ScrollPanel.superclass.constructor.call(this,D,B,E);this.el.dom.style.overflow="hidden";var C=this.el.wrap({cls:"x-scroller x-layout-inactive-content"});this.el.removeClass("x-layout-inactive-content");this.el.on("mousewheel",this.onWheel,this);var A=C.createChild({cls:"x-scroller-up",html:"&#160;"},this.el.dom);var F=C.createChild({cls:"x-scroller-down",html:"&#160;"});A.unselectable();F.unselectable();A.on("click",this.scrollUp,this);F.on("click",this.scrollDown,this);A.addClassOnOver("x-scroller-btn-over");F.addClassOnOver("x-scroller-btn-over");A.addClassOnClick("x-scroller-btn-click");F.addClassOnClick("x-scroller-btn-click");this.adjustments=[0,-(A.getHeight()+F.getHeight())];this.resizeEl=this.el;this.el=C;this.up=A;this.down=F};Ext.extend(Ext.ScrollPanel,Ext.ContentPanel,{increment:100,wheelIncrement:5,scrollUp:function(){this.resizeEl.scroll("up",this.increment,{callback:this.afterScroll,scope:this})},scrollDown:function(){this.resizeEl.scroll("down",this.increment,{callback:this.afterScroll,scope:this})},afterScroll:function(){var D=this.resizeEl;var A=D.dom.scrollTop,C=D.dom.scrollHeight,B=D.dom.clientHeight;this.up[A==0?"addClass":"removeClass"]("x-scroller-btn-disabled");this.down[C-A<=B?"addClass":"removeClass"]("x-scroller-btn-disabled")},setSize:function(){Ext.ScrollPanel.superclass.setSize.apply(this,arguments);this.afterScroll()},onWheel:function(A){var B=A.getWheelDelta();this.resizeEl.dom.scrollTop-=(B*this.wheelIncrement);this.afterScroll();A.stopEvent()},setContent:function(B,A){this.resizeEl.update(B,A)}});