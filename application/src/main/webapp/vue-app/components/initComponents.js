import ExoChatApp from './ExoChatApp.vue';
import ExoChatContact from './ExoChatContact.vue';
import ExoChatContactList from './ExoChatContactList.vue';
import ExoChatRoomParticipants from './ExoChatRoomParticipants.vue';
import ExoChatRoomDetail from './ExoChatRoomDetail.vue';
import ExoChatMessageList from './ExoChatMessageList.vue';
import ExoChatMessageDetail from './ExoChatMessageDetail.vue';
import ExoChatMessageComposer from './ExoChatMessageComposer.vue';


import ExoMiniChatApp from './ExoMiniChatApp.vue';
import ExoMiniChatNotifList from './ExoMiniChatNotifList.vue';
import ExoMiniChatRoom from './ExoMiniChatRoom.vue';
import ExoMiniChatNotifDetail from './ExoMiniChatNotifDetail.vue';

import ExoChatGlobalNotificationModal from './modal/ExoChatGlobalNotificationModal.vue';
import ExoChatComposerAppsModal from './modal/ExoChatComposerAppsModal.vue';
import ExoChatRoomNotificationModal from './modal/ExoChatRoomNotificationModal.vue';
import ExoChatRoomFormModal from './modal/ExoChatRoomFormModal.vue';

import ExoDropdownSelect from './ExoDropdownSelect.vue';
import ExoModal from './modal/ExoModal.vue';


const components = {
  'exo-chat-app': ExoChatApp,
  'exo-chat-notif-app': ExoMiniChatApp,
  'exo-modal': ExoModal,
  'exo-chat-contact': ExoChatContact,
  'exo-chat-contact-list': ExoChatContactList,
  'exo-chat-room-participants': ExoChatRoomParticipants,
  'exo-chat-room-detail': ExoChatRoomDetail,
  'exo-chat-message-list': ExoChatMessageList,
  'exo-chat-global-notification-modal': ExoChatGlobalNotificationModal,
  'exo-dropdown-select': ExoDropdownSelect,
  'exo-chat-room-form-modal': ExoChatRoomFormModal,
  'exo-chat-apps-modal': ExoChatComposerAppsModal,
  'exo-chat-message-detail': ExoChatMessageDetail,
  'exo-chat-message-composer': ExoChatMessageComposer,
  'exo-chat-room-notification-modal': ExoChatRoomNotificationModal,
  'exo-chat-notif-list': ExoMiniChatNotifList,
  'exo-chat-room': ExoMiniChatRoom,
  'exo-chat-notif-detail': ExoMiniChatNotifDetail
};

for(const key in components) {
  Vue.component(key, components[key]);
}