import { registerWebModule, NativeModule } from 'expo';

import { ExpoApplistModuleEvents } from './ExpoApplist.types';

class ExpoApplistModule extends NativeModule<ExpoApplistModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoApplistModule, 'ExpoApplistModule');
