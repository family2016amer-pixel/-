package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

  // Playgrounds
  val allPlaygrounds: Flow<List<Playground>> = db.playgroundDao().getAllPlaygrounds()
  suspend fun getPlaygroundById(id: Int) = db.playgroundDao().getPlaygroundById(id)
  suspend fun insertPlayground(playground: Playground) = db.playgroundDao().insertPlayground(playground)
  suspend fun updatePlayground(playground: Playground) = db.playgroundDao().updatePlayground(playground)
  suspend fun deletePlayground(playground: Playground) = db.playgroundDao().deletePlayground(playground)

  // Bookings
  val allBookings: Flow<List<Booking>> = db.bookingDao().getAllBookings()
  fun getBookingsByPhone(phone: String): Flow<List<Booking>> = db.bookingDao().getBookingsByPhone(phone)
  suspend fun insertBooking(booking: Booking) = db.bookingDao().insertBooking(booking)
  suspend fun updateBooking(booking: Booking) = db.bookingDao().updateBooking(booking)
  suspend fun deleteBooking(booking: Booking) = db.bookingDao().deleteBooking(booking)

  // Leagues
  val allLeagues: Flow<List<League>> = db.leagueDao().getAllLeagues()
  suspend fun getLeagueById(id: Int) = db.leagueDao().getLeagueById(id)
  suspend fun insertLeague(league: League) = db.leagueDao().insertLeague(league)
  suspend fun updateLeague(league: League) = db.leagueDao().updateLeague(league)
  suspend fun deleteLeague(league: League) = db.leagueDao().deleteLeague(league)

  // Teams
  val allTeams: Flow<List<Team>> = db.teamDao().getAllTeams()
  fun getTeamsForLeague(leagueId: Int): Flow<List<Team>> = db.teamDao().getTeamsForLeague(leagueId)
  suspend fun getTeamById(id: Int) = db.teamDao().getTeamById(id)
  suspend fun insertTeam(team: Team) = db.teamDao().insertTeam(team)
  suspend fun updateTeam(team: Team) = db.teamDao().updateTeam(team)
  suspend fun deleteTeam(team: Team) = db.teamDao().deleteTeam(team)

  // Team Invitations
  val allInvitations: Flow<List<TeamInvitation>> = db.teamInvitationDao().getAllInvitations()
  fun getInvitationsForUser(phone: String): Flow<List<TeamInvitation>> = db.teamInvitationDao().getInvitationsForUser(phone)
  fun getInvitationsForTeam(teamId: Int): Flow<List<TeamInvitation>> = db.teamInvitationDao().getInvitationsForTeam(teamId)
  suspend fun insertInvitation(invitation: TeamInvitation) = db.teamInvitationDao().insertInvitation(invitation)
  suspend fun updateInvitation(invitation: TeamInvitation) = db.teamInvitationDao().updateInvitation(invitation)
  suspend fun deleteInvitationById(id: Int) = db.teamInvitationDao().deleteInvitationById(id)

  // Friendly Matches
  val allMatches: Flow<List<FriendlyMatch>> = db.friendlyMatchDao().getAllMatches()
  suspend fun insertMatch(match: FriendlyMatch) = db.friendlyMatchDao().insertMatch(match)
  suspend fun updateMatch(match: FriendlyMatch) = db.friendlyMatchDao().updateMatch(match)

  // Academies
  val allAcademies: Flow<List<Academy>> = db.academyDao().getAllAcademies()
  suspend fun insertAcademy(academy: Academy) = db.academyDao().insertAcademy(academy)
  suspend fun updateAcademy(academy: Academy) = db.academyDao().updateAcademy(academy)

  // Academy Registrations
  val allRegistrations: Flow<List<AcademyRegistration>> = db.academyRegistrationDao().getAllRegistrations()
  fun getRegistrationsByPhone(phone: String): Flow<List<AcademyRegistration>> = db.academyRegistrationDao().getRegistrationsByPhone(phone)
  suspend fun insertRegistration(registration: AcademyRegistration) = db.academyRegistrationDao().insertRegistration(registration)
  suspend fun updateRegistration(registration: AcademyRegistration) = db.academyRegistrationDao().updateRegistration(registration)

  // Player Cards
  val allPublicCards: Flow<List<PlayerCard>> = db.playerCardDao().getAllPublicCards()
  val allCards: Flow<List<PlayerCard>> = db.playerCardDao().getAllCards()
  fun getCardsByPhone(phone: String): Flow<List<PlayerCard>> = db.playerCardDao().getCardsByPhone(phone)
  suspend fun insertCard(card: PlayerCard) = db.playerCardDao().insertCard(card)
  suspend fun updateCard(card: PlayerCard) = db.playerCardDao().updateCard(card)
  suspend fun deleteCard(card: PlayerCard) = db.playerCardDao().deleteCard(card)

  // Notifications
  val allNotifications: Flow<List<Notification>> = db.notificationDao().getAllNotifications()
  suspend fun insertNotification(notification: Notification) = db.notificationDao().insertNotification(notification)
  suspend fun markNotificationAsRead(id: Int) = db.notificationDao().markAsRead(id)
  suspend fun markAllNotificationsAsRead() = db.notificationDao().markAllAsRead()
  suspend fun clearAllNotifications() = db.notificationDao().clearAll()

  // Users
  val allUsers: Flow<List<User>> = db.userDao().getAllUsers()
  suspend fun getUserByPhone(phone: String) = db.userDao().getUserByPhone(phone)
  suspend fun getUserById(id: Int) = db.userDao().getUserById(id)
  suspend fun insertUser(user: User) = db.userDao().insertUser(user)
  suspend fun updateUser(user: User) = db.userDao().updateUser(user)
  suspend fun deleteUserByPhone(phone: String) = db.userDao().deleteUserByPhone(phone)

  // Home Banners
  val allBanners: Flow<List<HomeBanner>> = db.homeBannerDao().getAllBanners()
  suspend fun insertBanner(banner: HomeBanner) = db.homeBannerDao().insertBanner(banner)
  suspend fun updateBanner(banner: HomeBanner) = db.homeBannerDao().updateBanner(banner)
  suspend fun deleteBanner(banner: HomeBanner) = db.homeBannerDao().deleteBanner(banner)
}
