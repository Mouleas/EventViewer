import { module, test } from 'qunit';
import { setupTest } from 'event-jni/tests/helpers';

module('Unit | Route | loadTable', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:load-table');
    assert.ok(route);
  });
});
