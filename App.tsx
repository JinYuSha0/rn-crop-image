/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import {SafeAreaView, ScrollView, StyleSheet, Text} from 'react-native';
import ShowFC from './src/screens/showFC';
import ShowClass from './src/screens/showClass';

function App(): React.JSX.Element {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.container} contentContainerStyle={styles.grow}>
        <ShowFC />
        <Text>The following is the class component using mobx</Text>
        <ShowClass />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  grow: {
    flexGrow: 1,
  },
});

export default App;
