import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';

export default class IndexSelectorController extends Controller {

	@tracked systemPageNumber = 1;
	@tracked applicationPageNumber = 1;

	static pageNumberSystem = 1;
	static pageNumberApplication = 1;
	
	static maxPageSystem = 1;
	static maxPageApplication = 1;

	static statusForSystem = false;
	static statusForApplication = false;

	@action
	eventselector() {
		var sys = document.getElementById('eventSystem'),
		app = document.getElementById('eventApplication');

		if (sys.checked === true) {
			document.getElementById('SystemContainer').style.display = 'block';
			document.getElementById('ApplicationContainer').style.display = 'none';
		}
		if (app.checked === true) {
			document.getElementById('ApplicationContainer').style.display = 'block';
			document.getElementById('SystemContainer').style.display = 'none';
		}
		this.updateCount();
	}


	updateCount() {
		var sys = document.getElementById('eventSystem'),
		app = document.getElementById('eventApplication');

		if (sys.checked === true) {
			var count = 0;
			var d1 = $('#SystemArg1').val(),
				d2 = $('#SystemArg2').val();
			if (d1 && d2){
				$.ajax({
					type: 'POST',
					url: 'http://localhost:8080/Test12/event/getCount',
					data: { type: 'System', arg1: d1, arg2: d2 },
					success: function (res) {
						res = JSON.parse(res);
	
						document.getElementById('SystemCount').innerHTML = res.count;
						count = parseInt(res.count);

						var currSystemStatus = res.systemStatus;
						if (currSystemStatus.localeCompare("true") === 0){
							IndexSelectorController.statusForSystem = true;
						}
						var currApplicationStatus = res.applicationStatus;
						if (currApplicationStatus.localeCompare("true") === 0){
							IndexSelectorController.statusForApplication = true;
						}
						
						if (count % 20 === 0) {
							IndexSelectorController.maxPageSystem = Math.floor(count / 20);
						} 
						else {
							IndexSelectorController.maxPageSystem = Math.floor(count / 20) + 1;
						}
					},
				});
			}
			else {
				document.getElementById('SystemCount').innerHTML = '0';
			}
			
		} 
		else if (app.checked === true) {
			var count = 0;
			var d1 = $('#ApplicationArg1').val(),
				d2 = $('#ApplicationArg2').val();
			if (d1 && d2){
				$.ajax({
					type: 'POST',
					url: 'http://localhost:8080/Test12/event/getCount',
					data: { type: 'Application', arg1: d1, arg2: d2 },
					success: function (res) {
						res = JSON.parse(res);
						document.getElementById('ApplicationCount').innerHTML = res.count;
						count = parseInt(res.count);

						var currSystemStatus = res.systemStatus;
						if (currSystemStatus.localeCompare("true") === 0){
							IndexSelectorController.statusForSystem = true;
						}
						var currApplicationStatus = res.applicationStatus;
						if (currApplicationStatus.localeCompare("true") === 0){
							IndexSelectorController.statusForApplication = true;
						}

						if (count % 20 === 0) {
							IndexSelectorController.maxPageApplication = Math.floor(count / 20);
						} 
						else {
							IndexSelectorController.maxPageApplication = Math.floor(count / 20) + 1;
						}
					},
				});
			}
			else {
				document.getElementById('ApplicationCount').innerHTML = '0';
			}
		}
	}

	loadTableSystem() {
		var d1 = $('#SystemArg1').val();
		var d2 = $('#SystemArg2').val();
		var _this = this;
		_this.systemPageNumber = IndexSelectorController.pageNumberSystem;
		$.ajax({
			type: 'POST',
			url: 'http://localhost:8080/Test12/event/printData',
			data: { type: 'System', arg1: d1, arg2: d2, page: IndexSelectorController.pageNumberSystem },
			success: function (res) {
				_this.set('systemModel', res);
			},
		});
	}

	loadTableApplication() {
		var d1 = $('#ApplicationArg1').val();
		var d2 = $('#ApplicationArg2').val();
		var _this = this;
		_this.applicationPageNumber = IndexSelectorController.pageNumberApplication;
		$.ajax({
			type: 'POST',
			url: 'http://localhost:8080/Test12/event/printData',
			data: { type: 'Application', arg1: d1, arg2: d2, page: IndexSelectorController.pageNumberApplication },
			success: function (res) {
				_this.set('applicationModel', res);
			},
		});
	}

	
	@action
	nextPage() {
		var sys = document.getElementById('eventSystem'),
		app = document.getElementById('eventApplication');

		if (sys.checked === true) {
			IndexSelectorController.pageNumberSystem++;
			IndexSelectorController.pageNumberSystem = Math.max(1,Math.min(IndexSelectorController.pageNumberSystem,IndexSelectorController.maxPageSystem));
			this.loadTableSystem();
		} 
		else if (app.checked === true) {
			IndexSelectorController.pageNumberApplication++;
			IndexSelectorController.pageNumberApplication = Math.max(1,Math.min(IndexSelectorController.pageNumberApplication,IndexSelectorController.maxPageApplication));
			this.loadTableApplication();
		}
	}

	@action
	previousPage() {
		var sys = document.getElementById('eventSystem'),
		app = document.getElementById('eventApplication');

		if (sys.checked === true) {
			IndexSelectorController.pageNumberSystem--;
			IndexSelectorController.pageNumberSystem = Math.max(1,IndexSelectorController.pageNumberSystem);
			this.loadTableSystem();
		} 
		else if (app.checked === true) {
			IndexSelectorController.pageNumberApplication--;
			IndexSelectorController.pageNumberApplication = Math.max(1,IndexSelectorController.pageNumberApplication);
			this.loadTableApplication();
		}

	}

	@action
	SystemDateChange() {
		var currArg1 = document.getElementById('SystemArg1').value;
		this.sysArg2 = document.getElementById('SystemArg2').value;

		if (currArg1 !== this.sysArg1 || currArg1.localeCompare('') === 0) {
			document.getElementById('SystemArg2').value = '';
		}
		this.sysArg1 = currArg1;

		if (this.sysArg1 && this.sysArg2) {
			IndexSelectorController.pageNumberSystem = 1;
		}
		this.updateCount();
		this.loadTableSystem();
	}

	@action
	ApplicationDateChange() {
		var currArg1 = document.getElementById('ApplicationArg1').value;
		this.appArg2 = document.getElementById('ApplicationArg2').value;
		if (currArg1 !== this.appArg1 || currArg1.localeCompare('') === 0) {
			document.getElementById('ApplicationArg2').value = '';
		}
		this.appArg1 = currArg1;

		if (this.appArg1 && this.appArg2) {
			IndexSelectorController.pageNumberApplication = 1;
		}
		this.updateCount();
		this.loadTableApplication();
	}

	@action
	System() {
		var _this = this;

		var sys = document.getElementById('eventSystem'),
		app = document.getElementById('eventApplication');

		var sysArg1 = $('#SystemArg1').val(),
		sysArg2 = $('#SystemArg2').val();

		if (sysArg1 && sysArg2) {
			document.getElementById('SystemFetchButton').disabled = true;
			document.getElementById('SystemArg1').disabled = true;
			document.getElementById('SystemArg2').disabled = true;
			$.ajax({
				type: 'POST',
				url: 'http://localhost:8080/Test12/event/getDataBetweenDate',
				data: { type: 'System', arg1: sysArg1, arg2: sysArg2 },
				success: function () {
					IndexSelectorController.statusForSystem = false;
					var systime = setInterval(loadDataSystem, 1000);
					
					function loadDataSystem() {
						var currStatus = IndexSelectorController.statusForSystem;
						if (currStatus){
							clearInterval(systime);
							_this.loadTableSystem();
							_this.updateCount();
							document.getElementById('SystemFetchButton').disabled = false;
							document.getElementById('SystemArg1').disabled = false;
							document.getElementById('SystemArg2').disabled = false;
							app.disabled = false;
						}
						else {
							_this.updateCount();
							if ($("#SystemTable tr").length <= 20){
								_this.loadTableSystem();
							}
						}
					
					}
				},
			});
		}
		event.preventDefault();
	}

	@action
	Application() {
		var _this = this;

		var sys = document.getElementById('eventSystem'),
		app = document.getElementById('eventApplication');

		var appArg1 = $('#ApplicationArg1').val(),
		appArg2 = $('#ApplicationArg2').val();

		if (appArg1 && appArg2) {
			document.getElementById('ApplicationFetchButton').disabled = true;
			document.getElementById('ApplicationArg1').disabled = true;
			document.getElementById('ApplicationArg2').disabled = true;
			$.ajax({
				type: 'POST',
				url: 'http://localhost:8080/Test12/event/getDataBetweenDate',
				data: { type: 'Application', arg1: appArg1, arg2: appArg2 },
				success: function () {
					IndexSelectorController.statusForApplication = false;
					var apptime = setInterval(loadDataApplication, 1000);
					
					function loadDataApplication() {
						var currStatus = IndexSelectorController.statusForApplication;
						if (currStatus){
							clearInterval(apptime);
							_this.loadTableApplication();
							_this.updateCount();
							document.getElementById('ApplicationFetchButton').disabled = false;
							document.getElementById('ApplicationArg1').disabled = false;
							document.getElementById('ApplicationArg2').disabled = false;
							sys.disabled = false;
						}
						else {
							_this.updateCount();
							if ($("#ApplicationTable tr").length <= 20){
								_this.loadTableApplication();
							}
						}
					}
				},
			});
		}
		event.preventDefault();
	}
}
