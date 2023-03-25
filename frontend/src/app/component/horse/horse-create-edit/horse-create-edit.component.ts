import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {HttpErrorResponse} from '@angular/common/http';


export enum HorseCreateEditMode {
  create,
  edit,
  view
};

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  public readonly modes = HorseCreateEditMode;
  mode: HorseCreateEditMode = HorseCreateEditMode.create;

  horse: Horse = {
    name: '',
    description: '',
    // @ts-ignore
    dateOfBirth: null,
    sex: Sex.female,
  };


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
    this.route.paramMap.subscribe(() => {
      this.ngOnInit();
    });
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse';
      case HorseCreateEditMode.view:
        return 'Details of Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Update';
      case HorseCreateEditMode.view:
        return 'Edit';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }

  get modeIsView(): boolean {
    return this.mode === HorseCreateEditMode.view;
  }


  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'updated';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  fatherSuggestions = (input: string) =>
    input === '' ? of([]) : this.service.search({ name: input, sex: Sex.male, limit: 5 });

  motherSuggestions = (input: string) =>
    input === '' ? of([]) : this.service.search({ name: input, sex: Sex.female, limit: 5 });

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;

      if (this.mode === HorseCreateEditMode.edit || this.mode === HorseCreateEditMode.view) {
        const id = Number(this.route.snapshot.paramMap.get('id'));

        if (Number.isNaN(id)) {
          this.notification.error(`Invalid horse id given`);
          this.router.navigate(['/horses']);
          console.error('Error invalid horse id', this.route.snapshot.paramMap.get('id'));
          return;
        }

        this.service.get(id).subscribe({
          next: horse => {
            this.horse = horse;
          },
          error: (errorResponse: HttpErrorResponse) => {
            this.notification.error(`Could not find horse`);
            this.router.navigate(['/horses']);
            console.error('Error no horse found with id', this.route.snapshot.paramMap.get('id'));
            return;
          }
        });
      }
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatHorseName(horse: Horse | null | undefined): string {
    return horse == null ? '' : horse.name;
  }

  public onDelete(): void {
    if (!this.modeIsCreate && this.horse.id != null) {
      this.service.delete(this.horse.id).subscribe({
        next: () => {
          this.notification.success(`Horse ${this.horse.name} successfully deleted.`);
          this.router.navigate(['/horses']);
        },
        error: (errorResponse: HttpErrorResponse) => {
          this.notification.error(`Could not delete horse: ${errorResponse.error.errors}`);
          console.error('Error deleting horse', errorResponse.error.errors);
        }
      });
    }
  }

  public onSubmit(form: NgForm): void {
    if (this.modeIsView) {
      this.router.navigate(['/horses/edit', this.horse.id]);
      return;
    }

    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(this.horse);
          break;
        case HorseCreateEditMode.edit:
          observable = this.service.update(this.horse);
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: (errorResponse: HttpErrorResponse) => {
          let message = '';
          switch (errorResponse.status) {
            case 400:
              message = `Invalid data for creating ${this.horse.name}: ${errorResponse.error.errors}`;
              break;
            case 500:
              message = `Server error whilst creating ${this.horse.name}: ${errorResponse.error.errors}`;
              break;
            default:
              message = `Error while creating ${this.horse.name}: ${errorResponse.error.errors}`;
          }

          this.notification.error(message);
          console.error('Error creating horse', errorResponse);
        }
      });
    }
  }

}
