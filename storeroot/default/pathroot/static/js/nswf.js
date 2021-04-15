var FK_RPC = {


	call: function (path, name, args, callback) {
		var json = JSON.stringify(args);
		var result = {};

		$.ajax(path, {
			async: typeof (callback) == 'function',
			data: {
				"fk-rpc-fn": name,
				"fk-rpc-pvs": json
				//TODO : add fk-rpc-pagepath,now get from HTTP header REFERER

			},
			type: 'POST'
		}).always(function (datab, status) {
			if (status != 'success') {
				// alert("数据访问错误");
				return;
			}
			eval("var data=" + datab);
			if (data.ok) {
				if (typeof (callback) == 'function') {
					callback(data.result);
				} else {
					result.value = data.result;
				}
			} else {
				alert(data.errorMsg);
			}

		});
		return result.value;

	},
	invoke: function (name, args, cb) {
		return this.call("/handler/curpage", name, args, cb)
	}

};

var FK_FORM = {
	valuefields: [],
	comfields: [],
	dynfms: [],
	init: function (e) {
		$("[fk-ff-name]").each(function (i) {
			var n = $(this).attr("fk-ff-name");
			FK_FORM.valuefields[n] = $(this).attr("fk-ff-data"); // get value
			FK_FORM.comfields[n] = $(this).attr("fk-ff-com"); // get component
			if (!FK_FORM.comfields[n]) {
				FK_FORM.comfields[n] = FK_FORM.valuefields[n];
			}
		});
		//动态更换表单项
		$("[fk-dyn-field]").each(function (i) {
			var fn = $(this).attr("fk-dyn-field");
			FK_FORM.dynfms[fn] = $(this);

			var fkc = [];
			$(this).children().each(function () {
				fkc[$(this).attr("fk-dyn-value")] = $(this).detach();
			});

			$(FK_FORM.comfields[fn]).change(function () {
				FK_FORM.dynfms[fn].empty();
				var val = $(FK_FORM.valuefields[fn]).val();
				FK_FORM.dynfms[fn].append(fkc[val]);
			});
			$(FK_FORM.comfields[fn]).change();

		});
		//处理类似省份-城市这种联动,目前只支持select
		$("[fk-depends]").each(function () {
			var fn = $(this).attr("fk-depends");
			var ffmn = $(this);

			$(FK_FORM.comfields[fn]).change(function () {

				FK_RPC.call('/handler/rpcmisc?' + fn + "=" + $(this).val(), 'ffmValueList', [ffmn.attr('fk-fm-id'), $(this).val()], function (d) {

					ffmn.html('');
					for (var i = 0; i < d.length; i++) {
						var a = d[i];
						ffmn.append("<option value='" + a.value + "'>" + a.name + "</option>");
					}

				});

			});

		});
	},
	getValue: function (fieldName) {
		if (!this.valuefields[fieldName]) {
			alert("Cannot find field :" + fieldName);
			return null;
		}
		return $(this.valuefields[fieldName]).val();
	}

};