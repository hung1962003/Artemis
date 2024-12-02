import { Component, effect, input, output } from '@angular/core';
import { Posting, PostingType, SavedPostStatus } from 'app/entities/metis/posting.model';
import { faBookmark, faBoxArchive, faCheckSquare, faEllipsis, faHashtag, faLock } from '@fortawesome/free-solid-svg-icons';
import { ConversationType } from 'app/entities/metis/conversation/conversation.model';
import dayjs from 'dayjs/esm';

@Component({
    selector: 'jhi-posting-summary',
    templateUrl: './posting-summary.component.html',
    styleUrls: ['./posting-summary.component.scss'],
})
export class PostingSummaryComponent {
    readonly post = input<Posting>();
    readonly isShowSavedPostOptions = input<boolean>(false);

    readonly onChangeSavedPostStatus = output<SavedPostStatus>();
    readonly onNavigateToPost = output<Posting>();

    protected readonly ConversationType = ConversationType;
    protected readonly SavedPostStatus = SavedPostStatus;

    protected isAnswerPost = false;
    protected postingIsOfToday = false;
    protected isShowPosting = false;
    protected isShowSummary = false;
    protected isShowContent = false;

    // Icons
    readonly faLock = faLock;
    readonly faHashtag = faHashtag;
    readonly faCheckSquare = faCheckSquare;
    readonly faBookmark = faBookmark;
    readonly faBoxArchive = faBoxArchive;
    readonly faEllipsis = faEllipsis;

    constructor() {
        effect(() => {
            this.isShowPosting = this.post() !== undefined;
            this.isShowSummary =
                this.isShowPosting && this.post()!.conversation !== undefined && this.post()!.conversation!.type !== undefined && this.post()!.conversation!.title !== undefined;
            this.isShowContent = this.isShowPosting && this.post()!.author !== undefined && this.post()!.content !== undefined && this.post()!.postingType !== undefined;
            this.isAnswerPost = this.post()?.postingType === PostingType.ANSWER.valueOf();
            if (this.post()) {
                this.postingIsOfToday = dayjs().isSame(this.post()!.creationDate, 'day');
            }
        });
    }

    protected onStatusChangeClick(status: SavedPostStatus) {
        this.onChangeSavedPostStatus.emit(status);
    }

    protected onTriggerNavigateToPost() {
        if (this.post() === undefined) {
            return;
        }
        this.onNavigateToPost.emit(this.post()!);
    }
}