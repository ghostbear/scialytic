package di

import dagger.Component
import javax.inject.Singleton
import ui.main.MainViewModel

@Singleton
@Component(modules = [AudioModule::class, DataModule::class])
interface ViewModelComponent {
    val mainViewModel: MainViewModel
}