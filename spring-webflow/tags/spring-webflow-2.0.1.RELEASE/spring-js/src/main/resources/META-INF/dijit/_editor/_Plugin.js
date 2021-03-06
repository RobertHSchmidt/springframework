/*
	Copyright (c) 2004-2008, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/


if(!dojo._hasResource["dijit._editor._Plugin"]){dojo._hasResource["dijit._editor._Plugin"]=true;dojo.provide("dijit._editor._Plugin");dojo.require("dijit._Widget");dojo.require("dijit.Editor");dojo.require("dijit.form.Button");dojo.declare("dijit._editor._Plugin",null,{constructor:function(_1,_2){if(_1){dojo.mixin(this,_1);}this._connects=[];},editor:null,iconClassPrefix:"dijitEditorIcon",button:null,queryCommand:null,command:"",commandArg:null,useDefaultCommand:true,buttonClass:dijit.form.Button,getLabel:function(_3){return this.editor.commands[_3];},_initButton:function(_4){if(this.command.length){var _5=this.getLabel(this.command);var _6=this.iconClassPrefix+" "+this.iconClassPrefix+this.command.charAt(0).toUpperCase()+this.command.substr(1);if(!this.button){_4=dojo.mixin({label:_5,showLabel:false,iconClass:_6,dropDown:this.dropDown,tabIndex:"-1"},_4||{});this.button=new this.buttonClass(_4);}}},destroy:function(f){dojo.forEach(this._connects,dojo.disconnect);},connect:function(o,f,tf){this._connects.push(dojo.connect(o,f,this,tf));},updateState:function(){var _e=this.editor;var _c=this.command;if(!_e){return;}if(!_e.isLoaded){return;}if(!_c.length){return;}if(this.button){try{var _d=_e.queryCommandEnabled(_c);this.button.setAttribute("disabled",!_d);if(typeof this.button.checked=="boolean"){this.button.setAttribute("checked",_e.queryCommandState(_c));}}catch(e){console.debug(e);}}},setEditor:function(_e){this.editor=_e;this._initButton();if(this.command.length&&!this.editor.queryCommandAvailable(this.command)){if(this.button){this.button.domNode.style.display="none";}}if(this.button&&this.useDefaultCommand){this.connect(this.button,"onClick",dojo.hitch(this.editor,"execCommand",this.command,this.commandArg));}this.connect(this.editor,"onNormalizedDisplayChanged","updateState");},setToolbar:function(_f){if(this.button){_f.addChild(this.button);}}});}