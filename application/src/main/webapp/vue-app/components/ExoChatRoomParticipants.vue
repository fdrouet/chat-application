<template>
  <div v-if="contact && Object.keys(contact).length !== 0 && contact.type != 'u'" class="uiRoomUsersContainerArea">
    <div :class="{collapsed: isCollapsed && mq !== 'mobile'}" class="room-participants">
      <div v-exo-tooltip.left.body="tooltipCollapse" v-if="mq !=='mobile'" class="room-users-collapse-btn" @click="toggleCollapsed">
        <i class="uiIcon"></i>
      </div>
      <div class="room-participants-header no-user-selection">
        <div v-if="mq == 'mobile'" @click="backToConversation"><i class="uiIconGoBack"></i></div>
        <div v-exo-tooltip.left.body="tooltipParticipants" class="room-participants-filter">
          <div v-show="isCollapsed && mq !== 'mobile'" class="actionIcon" @click="toggleParticipantFilter">
            <i :class="{'all-participants': participantFilterClass}" class="uiIconChatMember uiIconChatLightGray"></i>
          </div>
        </div>
        <div v-show="!isCollapsed || mq === 'mobile'" class="room-participants-title">
          {{ $t("exoplatform.chat.participants.label") }}
          <span v-show="participantsCount > 0" class="nb-participants">({{ participantsCount }})</span>
        </div>
        <exo-dropdown-select v-show="!isCollapsed || mq === 'mobile'" position="right">
          <span slot="toggle">{{ filterByStatus[participantFilter] }}</span>
          <i slot="toggle" class="uiIconArrowDownMini"></i>
          <li v-for="(label, filter) in filterByStatus" slot="menu" :key="filter" @click="selectParticipantFilter(filter)"><a href="#"><i :class="{'not-filter': participantFilter !== filter}" class="uiIconTick"></i>{{ label }}</a></li>
        </exo-dropdown-select>
      </div>
      <div class="room-participants-list isList">
        <div v-for="contact in filteredParticipant" :key="contact.name" class="contact-list-item">
          <exo-chat-contact v-tiptip="contact.name" :list="true" :user-name="contact.name" :name="contact.fullname" :status="contact.status" type="u"></exo-chat-contact>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as chatServices from '../chatServices';
import * as chatWebStorage from '../chatWebStorage';
import {chatConstants} from '../chatConstants';

export default {
  // a directive for user popup plugin
  directives: {
    tiptip(el, binding, vnode) {
      $(el).find('.chat-contact-avatar').userPopup({
        restURL: chatConstants.PEOPLE_INFO_API,
        userId: binding.value,
        labels: {
          StatusTitle: vnode.context.$t('exoplatform.chat.user.popup.status'),
          Connect: vnode.context.$t('exoplatform.chat.user.popup.connect'),
          Confirm: vnode.context.$t('exoplatform.chat.user.popup.confirm'),
          CancelRequest: vnode.context.$t('exoplatform.chat.user.popup.cancel'),
          RemoveConnection: vnode.context.$t('exoplatform.chat.user.popup.remove.connection')
        },
        content: false,
        defaultPosition: 'left',
        keepAlive: true,
        maxWidth: '240px'
      });
    }
  },
  data : function() {
    return {
      isCollapsed: true,
      filterByStatus: {
        'All': this.$t('exoplatform.chat.contact.all'),
        'Online':  this.$t('exoplatform.chat.online'),
      },
      participantFilter: chatConstants.STATUS_FILTER_DEFAULT,
      /**
       * fullName: {string} full name of contact
       * isActive: {string} if the contact is of type user, this will be equals to "true" when the user is enabled
       * isFavorite: {Boolean} whether is favortie of current user or not
       * lastMessage: {string} Last message object with current user
       * room: {string} contact room id
       * status: {string} if the contact is of type user, this variable determines the user status (away, offline, available...)
       * timestamp: {number} contact update timestamp
       * type: {string} contact type, 'u' for user, 't' for team and 's' for space
       * unreadTotal: {number} unread total number of messages for this contact
       * user: {string} contact id, if user , username else team-{CONTACT_ID} or space-{CONTACT_ID}
       * Admins: {Array} Room admins list (only for room)
      */
      contact: null,
      /** Array of Participant
       * Participant {
       * name: {string} id of user
       * fullname: {string} full name of user
       * status: {string} if the contact is of type user, this variable determines the user status (away, offline, available...)
       * email: {string} email of user
       * }
       */
      participants: []
    };
  },
  computed: {
    participantsCount() {
      // subtract the current user
      return this.participants.length - 1;
    },
    filteredParticipant() {
      let listParticipants = [];
      const offline = ['invisible', 'offline'];
      listParticipants = this.participants.filter(participant => {
        return (this.participantFilter === 'All' ||  offline.indexOf(participant.status) < 0) && participant.name !== eXo.chat.userSettings.username;
      });
      return listParticipants.sort((p1, p2) => {
        if (p1.status === 'away' && p2.status === 'available' || p1.status === 'donotdisturb' && p2.status === 'available' || p1.status === 'donotdisturb' && p2.status === 'away' || offline.indexOf(p1.status) > -1 && offline.indexOf(p2.status) < 0) {
          return 1;
        }
      });
    },
    participantFilterClass() {
      return this.participantFilter === 'All';
    },
    tooltipParticipants() {
      return this.participantFilter === 'All' ? this.$t('exoplatform.chat.hide.users') : this.$t('exoplatform.chat.show.users');
    },
    tooltipCollapse() {
      return this.isCollapsed === true ? this.$t('exoplatform.chat.expand.users') : this.$t('exoplatform.chat.reduce.users');
    }
  },
  created() {
    document.addEventListener(chatConstants.ACTION_ROOM_SHOW_PARTICIPANTS, this.showParticipants);
    document.addEventListener(chatConstants.EVENT_ROOM_SELECTION_CHANGED, this.contactChanged);
    document.addEventListener(chatConstants.EVENT_USER_STATUS_CHANGED, this.contactStatusChanged);
    document.addEventListener(chatConstants.EVENT_ROOM_MEMBER_LEFT, this.leftRoom);
    this.participantFilter = chatWebStorage.getStoredParam(chatConstants.STORED_PARAM_STATUS_FILTER, chatConstants.STATUS_FILTER_DEFAULT);
  },
  destroyed() {
    document.removeEventListener(chatConstants.ACTION_ROOM_SHOW_PARTICIPANTS, this.showParticipants);
    document.removeEventListener(chatConstants.EVENT_ROOM_SELECTION_CHANGED, this.contactChanged);
    document.removeEventListener(chatConstants.EVENT_USER_STATUS_CHANGED, this.contactStatusChanged);
    document.removeEventListener(chatConstants.EVENT_ROOM_MEMBER_LEFT, this.leftRoom);
  },
  methods: {
    showParticipants() {
      this.isCollapsed = false;
    },
    toggleCollapsed() {
      this.isCollapsed = !this.isCollapsed;
    },
    selectParticipantFilter(filter) {
      chatWebStorage.setStoredParam(chatConstants.STORED_PARAM_STATUS_FILTER, filter);
      this.participantFilter = filter;
    },
    toggleParticipantFilter() {
      if (this.participantFilter === 'All') {
        this.selectParticipantFilter('Online');
      } else {
        this.selectParticipantFilter('All');
      }
    },
    leftRoom(e) {
      const message = e.detail ? e.detail: e;
      const sender = message.data && message.data.sender ? message.data.sender : message.sender;
      const roomLeft = message.data && message.data.room ? message.data.room : message.room;
      if (roomLeft !== this.contact.room) {
        return;
      }
      const roomIndex = this.participants.findIndex(contact => contact.name === sender);
      if (roomIndex >= 0) {
        this.participants.splice(roomIndex, 1);
      }
      this.$emit('particpants-loaded', this.participants);
    },
    contactChanged(e) {
      const contact = e.detail;
      this.contact = contact;
      this.participants = [];
      if (contact.type && contact.type !== 'u') {
        chatServices.getOnlineUsers().then(users => {
          chatServices.getRoomParticipants(eXo.chat.userSettings, contact).then( data => {
            this.$emit('particpants-loaded', data.users);
            this.participants = data.users.map(user => {
              // if user is not online, set its status as offline
              if(users.indexOf(user.name) < 0) {
                user.status = 'offline';
              }
              return user;
            });
          });
        });
      }
    },
    contactStatusChanged(e) {
      const contact = e.detail;
      const participantToChange = this.participants.find(participant => participant.name === contact.sender);
      if (participantToChange) {
        participantToChange.status = contact.data.status;
      }
    },
    backToConversation() {
      this.$emit('back-to-conversation');
    }
  }
};
</script>
