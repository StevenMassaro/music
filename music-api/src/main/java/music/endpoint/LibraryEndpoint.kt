package music.endpoint

import music.model.Library
import music.repository.ILibraryRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/library")
class LibraryEndpoint(private val libraryRepository: ILibraryRepository) {

    @GetMapping
    fun list(): List<Library> = libraryRepository.findAll()
}
