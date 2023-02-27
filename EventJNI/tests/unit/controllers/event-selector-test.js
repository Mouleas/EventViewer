import { module, test } from 'qunit';
import { setupTest } from 'event-jni/tests/helpers';

module('Unit | Controller | event-selector', function (hooks) {
  setupTest(hooks);

  // TODO: Replace this with your real tests.
  test('it exists', function (assert) {
    let controller = this.owner.lookup('controller:event-selector');
    assert.ok(controller);
  });
});
