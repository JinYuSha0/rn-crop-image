import {StyleSheet, View} from 'react-native';
import {IMG_SOURCE} from '../helper';
import CropImage from '../components/CropImage';
import React from 'react';
import AutoSizeImg from '../components/AutoSizeImage';

const ShowFC: React.FC<{}> = props => {
  return (
    <View style={styles.container}>
      <AutoSizeImg source={IMG_SOURCE.origin} width={300} />
      <View style={styles.padding} />
      <AutoSizeImg source={IMG_SOURCE.mask} width={300} />
      <View style={styles.padding} />
      <AutoSizeImg source={IMG_SOURCE.origin} width={300}>
        {(source, size) => (
          <CropImage style={[size]} source={source} mask={IMG_SOURCE.mask} />
        )}
      </AutoSizeImg>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    alignItems: 'center',
  },
  padding: {
    width: '100%',
    height: 16,
  },
});

export default ShowFC;
