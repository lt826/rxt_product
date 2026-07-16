// 商品卡片消息工具
// 约定：当一条消息的 content 是 JSON 字符串且 { type: 'goods', goodsId, ... } 时，
// 视为"商品卡片消息"，在聊天里渲染成带"加入购物车"按钮的商品卡片。
// 由于 tb_message.content 是 TEXT 类型，可直接存放这段 JSON，无需改库结构。

/**
 * 把一个商品对象打包成消息内容（农户发送时用）
 * @param {object} goods 后端 Goods 对象（getMyGoodsAPI 返回）
 * @returns {string} JSON 字符串
 */
export function buildGoodsCardContent(goods) {
  return JSON.stringify({
    type: 'goods',
    goodsId: goods.goodsId,
    title: goods.title || '商品',
    price: parseFloat(goods.price) || 0,
    picture: goods.picture || '',
    seller: goods.ownName || '',
    address: goods.address || ''
  })
}

/**
 * 解析消息内容为商品卡片对象；不是商品卡片则返回 null
 * @param {string} content
 * @returns {{goodsId:number,title:string,price:number,picture:string,seller:string,address:string}|null}
 */
export function parseGoodsCard(content) {
  if (!content || typeof content !== 'string') return null
  const s = content.trim()
  if (!(s.startsWith('{') && s.endsWith('}'))) return null
  try {
    const obj = JSON.parse(s)
    if (obj && obj.type === 'goods' && obj.goodsId != null) {
      return {
        goodsId: obj.goodsId,
        title: obj.title || '商品',
        price: parseFloat(obj.price) || 0,
        picture: obj.picture || '',
        seller: obj.seller || '',
        address: obj.address || ''
      }
    }
  } catch {
    /* 普通文本，非 JSON */
  }
  return null
}

/**
 * 会话列表预览文案：商品卡片显示为 "[商品] 标题"，其余原样返回
 * @param {string} content
 * @returns {string}
 */
export function goodsPreviewText(content) {
  const card = parseGoodsCard(content)
  return card ? `[商品] ${card.title}` : (content || '')
}

/**
 * 把商品卡片加入购物车（localStorage 的 rxt_cart）。
 * 结构与 buyer/Products.vue 的 addToCart 完全一致，保证购物车/结算流程通用。
 * 已存在的同 id 商品数量 +1，否则新增一条。
 * @param {{goodsId:number,title:string,price:number,seller:string,address:string}} card
 */
export function addGoodsCardToCart(card) {
  if (!card || card.goodsId == null) return false
  const cart = JSON.parse(localStorage.getItem('rxt_cart') || '[]')
  const idx = cart.findIndex(c => c.id === card.goodsId)
  if (idx >= 0) {
    cart[idx].quantity = (cart[idx].quantity || 1) + 1
  } else {
    cart.push({
      id: card.goodsId,
      title: card.title,
      price: card.price,
      quantity: 1,
      seller: card.seller,
      address: card.address
    })
  }
  localStorage.setItem('rxt_cart', JSON.stringify(cart))
  return true
}
