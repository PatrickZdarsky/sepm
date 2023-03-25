import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {debounceTime, map, of, Subscription} from 'rxjs';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {Horse, HorseSearch} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {HttpErrorResponse} from '@angular/common/http';
import {NgForm} from '@angular/forms';


@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('form', { static: true }) ngForm?: NgForm;

  horses: Horse[] = [];
  bannerError: string | null = null;
  searchData: HorseSearch = {};
  searchUpdate?: Subscription;

  constructor(
    private horseService: HorseService,
    private ownerService: OwnerService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadHorses();
  }

  ngAfterViewInit(): void {
    this.searchUpdate = this.ngForm?.valueChanges
      ?.pipe(debounceTime(250))
      .subscribe(this.reloadHorses.bind(this));
  }

  ngOnDestroy(): void {
    this.searchUpdate?.unsubscribe();
  }

  reloadHorses() {
    this.horseService.search(this.searchData)
      .subscribe({
        next: data => {
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  public formatAutocompleteInput(owner: string | null | undefined): string {
    return owner == null ? '' : owner;
  }

  ownerSuggestions = (input: string) =>
    input === ''
      ? of([])
      : this.ownerService.searchByName(input, 5)
        .pipe(
          map(owners =>
            owners.map(o => [o.firstName, o.lastName].filter(s => s.length > 0).join(' '))
          ));

  delete(id: number): void {
    this.horseService.delete(id).subscribe({
      next: () => {
        this.reloadHorses();
        this.notification.info('Horse deleted successfully.');
      },
      error: (errorResponse: HttpErrorResponse) => {
        this.notification.error(`Could not delete horse: ${errorResponse.error.errors}`);
      }
    });
  }
}
