define(function (require, exports, module) {

	var FK_RPC = require("rpc.js");

	var fkform = function (e) {
		this.valuefields = [];
		this.dynfms = [];
		this.com = [];
		this.init(e);
	}
	fkform.prototype.init = function (e) {
		var fm = this;
		$("[fk-ff-name]").each(function (i) {
			var n = $(this).attr("fk-ff-name");
			fm.valuefields[n] = $(this).attr("fk-ff-data"); // get value
			var cn = $(this).attr("fk-ff-com"); // get component
			if (!cn) {
				cn = fm.valuefields[n];
			}
			fm.com[n] = $(cn);
		});

		$("[fk-depends]").each(function () {
			var fn = $(this).attr("fk-depends");
			var ffmn = $(this);

			if (fm.com[fn]) {
				fm.com[fn].change(function () {

					FK_RPC.call('/handler/rpcmisc?' + fn + "=" + $(this).val(), 'ffmValueList', [ffmn.attr('fk-fm-id'), $(this).val()], function (d) {

						ffmn.html('');
						for (var i = 0; i < d.length; i++) {
							var a = d[i];
							ffmn.append("<option value='" + a.value + "'>" + a.name + "</option>");
						}

					});

				});
			}

		});

		$("[fk-dyn-field]").each(function (i) {

			var fn = $(this).attr("fk-dyn-field");

			fm.dynfms[fn] = $(this);

			var fkc = [];


			fm.com[fn].on("change", function () {
				fm.dynfms[fn].children().detach();
				var val = $(fm.valuefields[fn]).val();
				fm.dynfms[fn].append(fkc[val]);
			});
			$(this).children().each(function () {
				fkc[$(this).attr("fk-dyn-value")] = $(this).detach();

			});
			fm.com[fn].change();

		});

		//处理类似省份-城市这种联动,目前只支持select
	}
	fkform.prototype.getValue = function (fieldName) {
		var elv = this.valuefields[fieldName];
		var elc = this.com[fieldName];
		if (!elv) {
			if (elc && elc[0].getValue) {
				return elc[0].getValue();
			}
			alert("Cannot find field :" + fieldName);
			return null;
		}

		return $(this.valuefields[fieldName]).val();
	}


	return fkform;


});