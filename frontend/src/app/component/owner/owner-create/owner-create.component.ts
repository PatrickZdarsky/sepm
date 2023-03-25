import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {OwnerService} from 'src/app/service/owner.service';
import {HttpErrorResponse} from '@angular/common/http';
import {Owner} from 'src/app/dto/owner';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent implements OnInit {

  owner: Owner = {
    firstName: '',
    lastName: ''
  };
  constructor(
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
    this.route.paramMap.subscribe(() => {
      this.ngOnInit();
    });
  }

  ngOnInit(): void {
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.owner);
    if (form.valid) {
      if (this.owner.email && this.owner.email === '') {
        delete this.owner.email;
      }

      this.ownerService.create(this.owner).subscribe({
        next: data => {
          this.notification.success(`Owner ${this.owner.firstName} ${this.owner.lastName} successfully created.`);
          this.router.navigate(['/owners']);
        },
        error: (errorResponse: HttpErrorResponse) => {
          let message = '';
          switch (errorResponse.status) {
            case 400:
              message = `Invalid data for creating ${this.owner.firstName} ${this.owner.lastName}: ${errorResponse.error.errors}`;
              break;
            case 500:
              message = `Server error whilst creating ${this.owner.firstName} ${this.owner.lastName}: ${errorResponse.error.errors}`;
              break;
            default:
              message = `Error while creating ${this.owner.firstName} ${this.owner.lastName}: ${errorResponse.error.errors}`;
          }

          this.notification.error(message);
          console.error('Error creating owner', errorResponse);
        }
      });
    }
  }
}
