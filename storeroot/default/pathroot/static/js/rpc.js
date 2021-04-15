define(function (require, exports, module) {


	var FK_RPC = {

		call: function (path, name, args, callback) {
			var json = JSON.stringify(args);
			var result = {};

			$.ajax(path, {
				async: typeof (callback) == 'function',
				data: {
					"fk-rpc-fn": name,
					"fk-rpc-pvs": json
					//TODO : add fk-rpc-pagepath，now get from HTTP header REFERER

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
		},
		callpage: function (name, args, cb) {
			return this.call("/handler/curpage", name, args, cb)
		},
		callfragment: function (path, name, args, cb) {
			return this.call("/handler/curfragment?fk-rpc-pagepath=" + path, name, args, cb)
		}



	};


	exports.call = FK_RPC.call;
	exports.invoke = FK_RPC.invoke;
	exports.callfragment = FK_RPC.callfragment;
	exports.callpage = FK_RPC.callpage;


});