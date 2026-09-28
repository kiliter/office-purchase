<template>
  <span class="stamp" :class="kind">{{ text || "—" }}</span>
</template>

<script>
import { stampKind } from '../utils/dict'

/** 用印章样式展示申请或订单状态，文字始终可见。 */
export default {
  name: 'Stamp',
  props: {
    text: { type: String, default: '' }
  },
  computed: {
    kind() {
      return stampKind(this.text)
    }
  }
}
</script>
