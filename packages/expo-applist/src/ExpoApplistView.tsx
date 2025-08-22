import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoApplistViewProps } from './ExpoApplist.types';

const NativeView: React.ComponentType<ExpoApplistViewProps> =
  requireNativeView('ExpoApplist');

export default function ExpoApplistView(props: ExpoApplistViewProps) {
  return <NativeView {...props} />;
}
