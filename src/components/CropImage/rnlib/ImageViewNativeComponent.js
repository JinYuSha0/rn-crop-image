/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

// react-native/Libraries
// react-native/Libraries/Image

import type {ViewProps} from 'react-native/Libraries/Components/View/ViewPropTypes';
import type {
  HostComponent,
  PartialViewConfig,
} from 'react-native/Libraries/Renderer/shims/ReactNativeTypes';
import type {
  ColorValue,
  DangerouslyImpreciseStyle,
  ImageStyleProp,
} from 'react-native/Libraries/StyleSheet/StyleSheet';
import type {ResolvedAssetSource} from 'react-native/Libraries/Image/AssetSourceResolver';
import type {ImageProps} from 'react-native/Libraries/Image/ImageProps';

import * as NativeComponentRegistry from 'react-native/Libraries/NativeComponent/NativeComponentRegistry';
import {ConditionallyIgnoredEventHandlers} from 'react-native/Libraries/NativeComponent/ViewConfigIgnore';
import Platform from 'react-native/Libraries/Utilities/Platform';

type Props = $ReadOnly<{
  ...ImageProps,
  ...ViewProps,

  style?: ImageStyleProp | DangerouslyImpreciseStyle,

  // iOS native props
  tintColor?: ColorValue,

  // Android native props
  shouldNotifyLoadEvents?: boolean,
  src?:
    | ?ResolvedAssetSource
    | ?$ReadOnlyArray<?$ReadOnly<{uri?: ?string, ...}>>,
  headers?: ?{[string]: string},
  defaultSrc?: ?string,
  loadingIndicatorSrc?: ?string,
}>;

export const __INTERNAL_VIEW_CONFIG: PartialViewConfig =
  Platform.OS === 'android'
    ? {
        uiViewClassName: 'RCTCropImageView',
        bubblingEventTypes: {},
        directEventTypes: {
          topLoadStart: {
            registrationName: 'onLoadStart',
          },
          topProgress: {
            registrationName: 'onProgress',
          },
          topError: {
            registrationName: 'onError',
          },
          topLoad: {
            registrationName: 'onLoad',
          },
          topLoadEnd: {
            registrationName: 'onLoadEnd',
          },
        },
        validAttributes: {
          blurRadius: true,
          internal_analyticTag: true,
          resizeMode: true,
          tintColor: {
            process: require('react-native/Libraries/StyleSheet/processColor')
              .default,
          },
          borderBottomLeftRadius: true,
          borderTopLeftRadius: true,
          resizeMethod: true,
          src: true,
          borderRadius: true,
          headers: true,
          shouldNotifyLoadEvents: true,
          defaultSrc: true,
          overlayColor: {
            process: require('react-native/Libraries/StyleSheet/processColor')
              .default,
          },
          borderColor: {
            process: require('react-native/Libraries/StyleSheet/processColor')
              .default,
          },
          accessible: true,
          progressiveRenderingEnabled: true,
          fadeDuration: true,
          borderBottomRightRadius: true,
          borderTopRightRadius: true,
          loadingIndicatorSrc: true,
        },
      }
    : {
        uiViewClassName: 'RCTCropImageView',
        bubblingEventTypes: {},
        directEventTypes: {
          topLoadStart: {
            registrationName: 'onLoadStart',
          },
          topProgress: {
            registrationName: 'onProgress',
          },
          topError: {
            registrationName: 'onError',
          },
          topPartialLoad: {
            registrationName: 'onPartialLoad',
          },
          topLoad: {
            registrationName: 'onLoad',
          },
          topLoadEnd: {
            registrationName: 'onLoadEnd',
          },
        },
        validAttributes: {
          blurRadius: true,
          capInsets: {
            diff: require('react-native/Libraries/Utilities/differ/insetsDiffer'),
          },
          defaultSource: {
            process: require('react-native/Libraries/Image/resolveAssetSource'),
          },
          internal_analyticTag: true,
          resizeMode: true,
          source: true,
          tintColor: {
            process: require('react-native/Libraries/StyleSheet/processColor')
              .default,
          },
          ...ConditionallyIgnoredEventHandlers({
            onLoadStart: true,
            onLoad: true,
            onLoadEnd: true,
            onProgress: true,
            onError: true,
            onPartialLoad: true,
          }),
        },
      };

const ImageViewNativeComponent: HostComponent<Props> =
  NativeComponentRegistry.get<Props>(
    'RCTCropImageView',
    () => __INTERNAL_VIEW_CONFIG,
  );

export default ImageViewNativeComponent;
