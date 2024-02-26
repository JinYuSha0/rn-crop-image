import {type ImageSourcePropType, Image} from 'react-native';
import React, {useCallback, useMemo} from 'react';

export type Size = {
  width: number;
  height: number;
};

export default function useImgSize(source: ImageSourcePropType) {
  const size = useMemo<Size>(() => {
    const {width, height} = Image.resolveAssetSource(source);
    return {width, height};
  }, [source]);
  const scaleByWidth = useCallback<(width: number) => Size>(
    (width: number) => {
      return {width, height: (width / size.width) * size.height};
    },
    [size],
  );
  const scaleByHeight = useCallback<(width: number) => Size>(
    (height: number) => {
      return {height, width: (height / size.height) * size.width};
    },
    [size],
  );
  return {
    size,
    scaleByWidth,
    scaleByHeight,
  };
}
