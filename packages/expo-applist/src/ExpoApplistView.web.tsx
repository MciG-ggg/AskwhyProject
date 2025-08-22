import * as React from 'react';

import { ExpoApplistViewProps } from './ExpoApplist.types';

export default function ExpoApplistView(props: ExpoApplistViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
