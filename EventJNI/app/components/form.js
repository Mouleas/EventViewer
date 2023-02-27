import Component from '@glimmer/component';
import { action } from '@ember/object';

export default class FormComponent extends Component {
  @action
  nextPage() {
    this.args.nextPage();
  }

  @action
  previousPage() {
    this.args.previousPage();
  }

  @action
  SystemDateChange() {
    this.args.SystemDateChange();
  }

  @action
  System() {
    this.args.System();
  }

  @action
  ApplicationDateChange() {
    this.args.ApplicationDateChange();
  }

  @action
  Application() {
    this.args.Application();
  }
}
