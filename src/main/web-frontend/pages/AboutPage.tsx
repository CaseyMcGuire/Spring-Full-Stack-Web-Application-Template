import * as React from "react";
import * as stylex from '@stylexjs/stylex';


const styles = stylex.create({
  body: {
    color: 'green'
  }
})
export default function AboutPage() {
  return (
    <div {...styles.body}>About</div>
  )
}