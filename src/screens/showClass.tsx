import {
  type ImageResolvedAssetSource,
  type ImageSourcePropType,
  StyleSheet,
  View,
  Image,
} from 'react-native';
import {IMG_SOURCE} from '../helper';
import {makeAutoObservable, action, observable, runInAction} from 'mobx';
import {observer} from 'mobx-react';
import React from 'react';
import AutoSizeImg from '../components/AutoSizeImage';
import CropImageModule from '../native/cropImage';

class ImageLoader {
  @observable loaded: ImageResolvedAssetSource[] = [];

  constructor() {
    makeAutoObservable(this, {
      loaded: observable,
      add: action,
    });
  }

  @action
  async add(origin: ImageSourcePropType, mask: ImageSourcePropType) {
    const res = await CropImageModule.crop(origin, mask);
    runInAction(() => {
      this.loaded.push(res);
    });
  }
}

interface ShowClassProps {
  loader: ImageLoader;
}

@observer
class ShowClass extends React.Component<ShowClassProps> {
  componentDidMount() {
    this.props.loader.add(IMG_SOURCE.origin, IMG_SOURCE.mask);
  }

  componentWillUnmount() {
    // CropImageModule.clearTemp();
  }

  render() {
    const {
      loader: {loaded},
    } = this.props;
    return (
      <View style={styles.container}>
        <AutoSizeImg source={IMG_SOURCE.origin} width={300} />
        <View style={styles.padding} />
        <AutoSizeImg source={IMG_SOURCE.mask} width={300} />
        <View style={styles.padding} />
        {loaded.map(source => (
          <View key={source.uri}>
            <AutoSizeImg source={IMG_SOURCE.origin} width={300}>
              {(_, size) => <Image source={{...source}} style={[size]} />}
            </AutoSizeImg>
            <View style={styles.padding} />
          </View>
        ))}
      </View>
    );
  }
}

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

const loader = new ImageLoader();
export default () => <ShowClass loader={loader} />;
