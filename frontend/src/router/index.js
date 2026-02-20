import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ItemList from '../views/ItemList.vue'
import ItemAdd from '../views/ItemAdd.vue'
import ItemDetail from '../views/ItemDetail.vue'
import Rooms from '../views/Rooms.vue'
import Stats from '../views/Stats.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/items',
    name: 'ItemList',
    component: ItemList
  },
  {
    path: '/items/add',
    name: 'ItemAdd',
    component: ItemAdd
  },
  {
    path: '/items/:id',
    name: 'ItemDetail',
    component: ItemDetail,
    props: true
  },
  {
    path: '/rooms',
    name: 'Rooms',
    component: Rooms
  },
  {
    path: '/stats',
    name: 'Stats',
    component: Stats
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
