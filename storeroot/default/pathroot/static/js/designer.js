define(function (require, exports, module) {


    var rpc = require("rpc");
    var vm = new Vue({
        el: "#main",
        data: {},
        methods: {}

    });
    var d = {

        editingEle: null,
        trackRoot: null,


        stopEdit: function () {
            if (this.editingEle) {
                this.editingEle.removeAttr("contentEditable");
                if (this.editingEle.src) {
                    this.editingEle.replaceWith(this.editingEle.src)
                }
                this.editingEle = null;
            }


        },

        edit: function (ele) {
            if (this.editingEle == ele) {
                aler("equal");
                return;
            }

            this.stopEdit();


            ele.attr("contentEditable", "true");
            this.editingEle = ele;
            //evt.preventDefault();


        },
        isFrameEditable: function () {
            return $("#btn-edit").hasClass("btn-primary");
        },
        clickHandler: function (evt) {
            evt.stopPropagation();
            evt.preventDefault();
            var e = $(this);
            var eid = e.attr("eleid-design");
            var epath = e.attr("elepath-design");
            //alert(e.html)

            /*var es = rpc.invoke("eleSrc", [epath, eid]);

             var a = $(es)
             a.src = e;
             e.replaceWith(a)
             e = a;*/
            d.edit(e);
            d.updateTrack(e, ".fk-edit");
            e.focus();

//			e.popover({title: epath, trigger: "click  hover focus", placement: "auto"})
//			e.popover('show')

        },
        updateEleInfo: function (ele) {

        },
        overHandler: function (evt) {

            var e = $(this);
            var eid = e.attr("eleid-design");
            var epath = e.attr("elepath-design");
            //d.edit(e);
            //$(".fk-focus", $("#pageFrame")[0].contentDocument).css({top: e.offset().top, left: e.offset().left, width: e.outerWidth(), height: e.outerHeight()});
            d.updateTrack(e, ".fk-focus");
//			e.popover({title: epath, trigger: "click  hover focus", placement: "auto"})
//			e.popover('show')
            evt.stopPropagation();
        },
//		outHandler: function (evt) {
//
//			$(".fk-focus", this.trackRoot).css(
//				{position: "absolute", "border-style": "none",  top: 0, left: 0, width: 0, height: 10});
//			evt.stopPropagation();
//		},
        updateTrack: function (e, type) {
            var isFocus = type == ".fk-focus";
            $(type, this.trackRoot).css(
                {
                    position: "absolute",
                    "border-color": (isFocus ? "blue" : "red"),
                    "border-style": ( isFocus ? "dotted" : "solid"),
                    "border-width": "1px",
                    top: e.offset().top - 1,
                    left: e.offset().left - 1,
                    width: e.outerWidth() + 1,
                    height: e.outerHeight() + 1,
                    "z-index": 65535
                });
            $(".t,.b", this.trackRoot).height("0px");
            $(type + ".b", this.trackRoot).css({top: e.offset().top + e.outerHeight() - 1});
            $(type + ".r", this.trackRoot).css({left: e.offset().left + e.outerWidth() - 1});
            $(".l,.r", this.trackRoot).width("0px");

            var box = $(type + "-box", this.trackRoot);

            var boxtop = e.offset().top;
            box.text(boxtop)
            box.css({
                "z-index": 65535,
                position: "absolute",
                left: e.offset().left - 1,
                top: boxtop - box.outerHeight() > 100 ? boxtop - box.outerHeight() : boxtop + e.outerHeight()

            });
            if (isFocus) {


            } else {

            }


        },
        updateFrameEditable: function () {
            var contentDocument = $("#pageFrame")[0].contentDocument;


            if (this.isFrameEditable()) {

                $("body", contentDocument).on("mouseenter", "[eleid-design]", this.overHandler);
                $("body", contentDocument).on("click", "[eleid-design]", this.clickHandler);

                //	alert($(".fk-track").html())

                $("body", contentDocument).append(
                    $(".fk-track").clone());
                this.trackRoot = $(".fk-track", contentDocument);
            } else {
                $("body", contentDocument).off("mouseenter", "[eleid-design]", this.overHandler);
                $("body", contentDocument).off("click", "[eleid-design]", this.clickHandler);

                if (this.trackRoot) {
                    this.trackRoot.remove();
                    this.trackRoot = null;
                }

                this.stopEdit();
            }
        },
        toggleFrameEditable: function () {
            $("#btn-edit").toggleClass("btn-primary");
            d.updateFrameEditable();


        },

        init: function () {
            $("#btn-edit").on("click", d.toggleFrameEditable);
            $("#main").on("mouseenter", function () {

                if (d.isFrameEditable()) {
                    $("#btn-edit ~ button").show()

                }
            });
            $("#main").on("mouseleave", function () {
                $("#btn-edit ~ button").hide()
            });
            $("#pageFrame").on("load", function () {
                d.updateFrameEditable();

            })

        }

    };
    return d;


})
